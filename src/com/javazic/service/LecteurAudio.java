package com.javazic.service;

import com.javazic.model.Morceau;
import com.javazic.util.FormatUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lecteur audio pilotable et reutilisable.
 * Utilise JavaFX par reflection si disponible, sinon simule la lecture.
 */
public class LecteurAudio {

    private enum CommandeTransport {
        NONE,
        NEXT,
        PREVIOUS,
        STOP
    }

    private final Object stateLock = new Object();

    private volatile Object mediaPlayer;
    private volatile boolean javafxDisponible;
    private volatile boolean sessionActive;
    private volatile boolean enPause;
    private volatile int indexCourant = -1;
    private volatile CommandeTransport commandeEnAttente = CommandeTransport.NONE;
    private volatile Thread threadLecture;
    private volatile long sessionGeneration;
    private volatile long mediaPlayerGeneration = -1L;
    private volatile int secondesEcoulees;
    private volatile int dureeTotaleCourante;
    private boolean afficherConsole = true;

    private List<Morceau> fileLecture = new ArrayList<>();

    public LecteurAudio() {
        try {
            Class.forName("javafx.scene.media.MediaPlayer");
            javafxDisponible = true;
        } catch (ClassNotFoundException e) {
            javafxDisponible = false;
        }
    }

    public static void initialiserJavaFX() {
        try {
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            Method startup = platformClass.getMethod("startup", Runnable.class);
            startup.invoke(null, (Runnable) () -> {});
        } catch (Exception ignored) {
            // JavaFX absent ou deja initialise.
        }
    }

    public void play(List<Morceau> queue, int startIndex) {
        stop();

        if (queue == null || queue.isEmpty()) {
            return;
        }

        final long generation;
        synchronized (stateLock) {
            generation = sessionGeneration + 1L;
            sessionGeneration = generation;
            this.fileLecture = new ArrayList<>(queue);
            this.indexCourant = Math.max(0, Math.min(startIndex, queue.size() - 1));
            this.sessionActive = true;
            this.enPause = false;
            this.commandeEnAttente = CommandeTransport.NONE;

            threadLecture = new Thread(() -> boucleLecture(generation), "javazic-player");
            threadLecture.setDaemon(true);
            threadLecture.start();
        }
    }

    public boolean togglePause() {
        synchronized (stateLock) {
            if (!sessionActive) {
                return false;
            }

            enPause = !enPause;
            Object player = mediaPlayer;
            if (player != null && javafxDisponible) {
                if (enPause) {
                    pauseMediaPlayer(player);
                } else {
                    playMediaPlayer(player);
                }
            }
            return true;
        }
    }

    public boolean next() {
        synchronized (stateLock) {
            if (!sessionActive || indexCourant >= fileLecture.size() - 1) {
                return false;
            }
            enPause = false;
            commandeEnAttente = CommandeTransport.NEXT;
        }
        arreterMediaPlayerActuel();
        return true;
    }

    public boolean previous() {
        synchronized (stateLock) {
            if (!sessionActive || indexCourant <= 0) {
                return false;
            }
            enPause = false;
            commandeEnAttente = CommandeTransport.PREVIOUS;
        }
        arreterMediaPlayerActuel();
        return true;
    }

    public void stop() {
        Thread threadAJoindre;
        synchronized (stateLock) {
            if (!sessionActive && threadLecture == null) {
                return;
            }
            sessionActive = false;
            enPause = false;
            commandeEnAttente = CommandeTransport.STOP;
            threadAJoindre = threadLecture;
        }

        arreterMediaPlayerActuel();

        if (threadAJoindre != null && threadAJoindre != Thread.currentThread()) {
            threadAJoindre.interrupt();
            if (!estSurFxApplicationThread()) {
                try {
                    threadAJoindre.join(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        synchronized (stateLock) {
            if (threadLecture == threadAJoindre) {
                threadLecture = null;
            }
            if (commandeEnAttente == CommandeTransport.STOP) {
                commandeEnAttente = CommandeTransport.NONE;
            }
        }
    }

    public boolean isPlaying() {
        return sessionActive;
    }

    public boolean isPaused() {
        return enPause;
    }

    public Morceau getCurrentTrack() {
        synchronized (stateLock) {
            if (!sessionActive || indexCourant < 0 || indexCourant >= fileLecture.size()) {
                return null;
            }
            return fileLecture.get(indexCourant);
        }
    }

    public int getSecondesEcoulees() { return secondesEcoulees; }
    public int getDureeTotaleCourante() { return dureeTotaleCourante; }
    public void setAfficherConsole(boolean afficher) { this.afficherConsole = afficher; }

    private void boucleLecture(long generation) {
        try {
            while (true) {
                Morceau courant;
                synchronized (stateLock) {
                    if (!sessionActive || sessionGeneration != generation
                            || indexCourant < 0 || indexCourant >= fileLecture.size()) {
                        break;
                    }
                    courant = fileLecture.get(indexCourant);
                }

                CommandeTransport resultat = jouerMorceau(courant, generation);
                System.out.println();

                synchronized (stateLock) {
                    if (!sessionActive || sessionGeneration != generation
                            || resultat == CommandeTransport.STOP) {
                        break;
                    }

                    if (resultat == CommandeTransport.PREVIOUS) {
                        if (indexCourant > 0) {
                            indexCourant--;
                            continue;
                        }
                        continue;
                    }

                    if (indexCourant < fileLecture.size() - 1) {
                        indexCourant++;
                    } else {
                        sessionActive = false;
                        break;
                    }
                }
            }
        } finally {
            synchronized (stateLock) {
                if (threadLecture == Thread.currentThread()) {
                    threadLecture = null;
                }
                if (sessionGeneration == generation) {
                    sessionActive = false;
                    enPause = false;
                    commandeEnAttente = CommandeTransport.NONE;
                    mediaPlayer = null;
                    mediaPlayerGeneration = -1L;
                }
            }
        }
    }

    private CommandeTransport jouerMorceau(Morceau morceau, long generation) {
        Object player = null;
        boolean lectureReelle = false;

        try {
            if (javafxDisponible && morceau.getStreamUrl() != null && !morceau.getStreamUrl().isEmpty()) {
                player = creerMediaPlayer(morceau.getStreamUrl());
                enregistrerMediaPlayer(player, generation);
                playMediaPlayer(player);
                lectureReelle = true;
            }
        } catch (Exception e) {
            player = null;
            libererMediaPlayerSiCourant(player, generation);
        }

        int dureeMax = determinerDureeLecture(morceau);
        dureeTotaleCourante = dureeMax;
        secondesEcoulees = 0;
        long dureeMs = Math.max(1000L, dureeMax * 1000L);
        long elapsedMs = 0L;
        long dernierTick = System.currentTimeMillis();

        try {
            while (estSessionCourante(generation)) {
                CommandeTransport commande = consommerCommande();
                if (commande != CommandeTransport.NONE) {
                    return commande;
                }

                if (enPause) {
                    dernierTick = System.currentTimeMillis();
                    dormir(120);
                    continue;
                }

                long maintenant = System.currentTimeMillis();
                elapsedMs += (maintenant - dernierTick);
                dernierTick = maintenant;

                secondesEcoulees = (int) Math.min(dureeMax, elapsedMs / 1000L);
                afficherProgression(secondesEcoulees, dureeMax);

                if (elapsedMs >= dureeMs) {
                    break;
                }

                dormir(150);
            }
            return CommandeTransport.NONE;
        } finally {
            if (lectureReelle) {
                fermerPlayer(player);
            }
            libererMediaPlayerSiCourant(player, generation);
        }
    }

    private int determinerDureeLecture(Morceau morceau) {
        int duree = Math.max(1, morceau.getDuree());
        if (morceau.estDistant()) {
            return Math.min(duree, 30);
        }
        return Math.min(duree, 5);
    }

    private CommandeTransport consommerCommande() {
        synchronized (stateLock) {
            CommandeTransport commande = commandeEnAttente;
            if (commande != CommandeTransport.NONE) {
                commandeEnAttente = CommandeTransport.NONE;
            }
            return commande;
        }
    }

    private void afficherProgression(int secondesEcoulees, int dureeTotale) {
        if (!afficherConsole) return;
        int pct = dureeTotale > 0 ? (secondesEcoulees * 100 / dureeTotale) : 0;
        pct = Math.min(100, Math.max(0, pct));
        int barLen = 30;
        int filled = barLen * pct / 100;

        StringBuilder barre = new StringBuilder("[");
        for (int i = 0; i < barLen; i++) {
            barre.append(i < filled ? "=" : " ");
        }
        barre.append("] ");
        barre.append(FormatUtil.formaterDuree(secondesEcoulees));
        barre.append(" / ");
        barre.append(FormatUtil.formaterDuree(dureeTotale));
        barre.append("  ").append(pct).append("%");

        if (enPause) {
            barre.append("  [PAUSE]");
        }

        System.out.print("\r" + barre);
    }

    private void dormir(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void arreterMediaPlayerActuel() {
        Object player;
        synchronized (stateLock) {
            player = mediaPlayer;
            mediaPlayer = null;
            mediaPlayerGeneration = -1L;
        }
        fermerPlayer(player);
    }

    private void enregistrerMediaPlayer(Object player, long generation) {
        synchronized (stateLock) {
            mediaPlayer = player;
            mediaPlayerGeneration = generation;
        }
    }

    private void libererMediaPlayerSiCourant(Object player, long generation) {
        if (player == null) {
            return;
        }
        synchronized (stateLock) {
            if (mediaPlayer == player && mediaPlayerGeneration == generation) {
                mediaPlayer = null;
                mediaPlayerGeneration = -1L;
            }
        }
    }

    private boolean estSessionCourante(long generation) {
        return sessionActive && sessionGeneration == generation;
    }

    private Object creerMediaPlayer(String url) throws Exception {
        AtomicReference<Object> playerRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        executerSurJavaFxEtAttendre(() -> {
            try {
                Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
                Constructor<?> mediaCtor = mediaClass.getConstructor(String.class);
                Object media = mediaCtor.newInstance(url);

                Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");
                Constructor<?> playerCtor = mediaPlayerClass.getConstructor(mediaClass);
                playerRef.set(playerCtor.newInstance(media));
            } catch (Exception e) {
                errorRef.set(e);
            }
        });

        if (errorRef.get() != null) {
            throw errorRef.get();
        }
        return playerRef.get();
    }

    private void playMediaPlayer(Object player) {
        invoquerMediaPlayer(player, "play");
    }

    private void pauseMediaPlayer(Object player) {
        invoquerMediaPlayer(player, "pause");
    }

    private void invoquerMediaPlayer(Object player, String methode) {
        if (player == null) {
            return;
        }
        invoquerMediaPlayerSansAttendre(player, methode);
    }

    private void fermerPlayer(Object player) {
        if (player == null) {
            return;
        }
        invoquerMediaPlayerSansAttendre(player, "stop");
        invoquerMediaPlayerSansAttendre(player, "dispose");
    }

    private void invoquerMediaPlayerSansAttendre(Object player, String methode) {
        try {
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            Method isFxThread = platformClass.getMethod("isFxApplicationThread");
            boolean surFx = (Boolean) isFxThread.invoke(null);
            if (surFx) {
                player.getClass().getMethod(methode).invoke(player);
            } else {
                Method runLater = platformClass.getMethod("runLater", Runnable.class);
                runLater.invoke(null, (Runnable) () -> {
                    try {
                        player.getClass().getMethod(methode).invoke(player);
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
    }

    private boolean estSurFxApplicationThread() {
        try {
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            Method isFxThread = platformClass.getMethod("isFxApplicationThread");
            return (Boolean) isFxThread.invoke(null);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void executerSurJavaFxEtAttendre(Runnable action) throws Exception {
        Class<?> platformClass = Class.forName("javafx.application.Platform");
        Method isFxThread = platformClass.getMethod("isFxApplicationThread");
        boolean dejaSurFxThread = (Boolean) isFxThread.invoke(null);
        if (dejaSurFxThread) {
            action.run();
            return;
        }

        Method runLater = platformClass.getMethod("runLater", Runnable.class);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        runLater.invoke(null, (Runnable) () -> {
            try {
                action.run();
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });

        latch.await(3, TimeUnit.SECONDS);
        if (errorRef.get() != null) {
            throw errorRef.get();
        }
    }
}
