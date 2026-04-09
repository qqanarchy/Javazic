package com.javazic.service;

import com.javazic.model.Album;
import com.javazic.model.Artiste;
import com.javazic.model.Morceau;
import com.javazic.model.Source;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

/**
 * Registre central des entites distantes.
 * Genere des IDs internes negatifs uniques pour toutes les sources externes.
 */
public class RemoteMediaRegistry {

    private final AtomicInteger nextRemoteId = new AtomicInteger(-1);
    private final Map<String, Artiste> artistesByKey = new LinkedHashMap<>();
    private final Map<String, Album> albumsByKey = new LinkedHashMap<>();
    private final Map<String, Morceau> morceauxByKey = new LinkedHashMap<>();
    private final Map<Integer, Artiste> artistesById = new LinkedHashMap<>();
    private final Map<Integer, Album> albumsById = new LinkedHashMap<>();
    private final Map<Integer, Morceau> morceauxById = new LinkedHashMap<>();

    public synchronized Artiste getOrCreateArtiste(Source source, String sourceId,
                                                   IntFunction<Artiste> factory) {
        String key = buildKey("artist", source, sourceId);
        Artiste existing = artistesByKey.get(key);
        if (existing != null) {
            return existing;
        }

        int id = nextRemoteId.getAndDecrement();
        Artiste artiste = factory.apply(id);
        artiste.setSource(source);
        artiste.setSourceId(sourceId);
        artistesByKey.put(key, artiste);
        artistesById.put(id, artiste);
        return artiste;
    }

    public synchronized Album getOrCreateAlbum(Source source, String sourceId,
                                               IntFunction<Album> factory) {
        String key = buildKey("album", source, sourceId);
        Album existing = albumsByKey.get(key);
        if (existing != null) {
            return existing;
        }

        int id = nextRemoteId.getAndDecrement();
        Album album = factory.apply(id);
        album.setSource(source);
        album.setSourceId(sourceId);
        albumsByKey.put(key, album);
        albumsById.put(id, album);
        return album;
    }

    public synchronized Morceau getOrCreateMorceau(Source source, String sourceId,
                                                   IntFunction<Morceau> factory) {
        String key = buildKey("track", source, sourceId);
        Morceau existing = morceauxByKey.get(key);
        if (existing != null) {
            return existing;
        }

        int id = nextRemoteId.getAndDecrement();
        Morceau morceau = factory.apply(id);
        morceau.setSource(source);
        morceau.setSourceId(sourceId);
        morceauxByKey.put(key, morceau);
        morceauxById.put(id, morceau);
        return morceau;
    }

    public synchronized Artiste getArtiste(int id) {
        return artistesById.get(id);
    }

    public synchronized Album getAlbum(int id) {
        return albumsById.get(id);
    }

    public synchronized Morceau getMorceau(int id) {
        return morceauxById.get(id);
    }

    public synchronized void registerExistingArtiste(Artiste artiste) {
        if (artiste == null || !artiste.estDistant() || artiste.getSourceId().isEmpty()) {
            return;
        }

        String key = buildKey("artist", artiste.getSource(), artiste.getSourceId());
        artistesByKey.putIfAbsent(key, artiste);
        artistesById.putIfAbsent(artiste.getId(), artiste);
        ajusterProchainId(artiste.getId());
    }

    public synchronized void registerExistingAlbum(Album album) {
        if (album == null || !album.estDistant() || album.getSourceId().isEmpty()) {
            return;
        }

        registerExistingArtiste(album.getArtiste());

        String key = buildKey("album", album.getSource(), album.getSourceId());
        albumsByKey.putIfAbsent(key, album);
        albumsById.putIfAbsent(album.getId(), album);
        ajusterProchainId(album.getId());
    }

    public synchronized void registerExistingMorceau(Morceau morceau) {
        if (morceau == null || !morceau.estDistant() || morceau.getSourceId().isEmpty()) {
            return;
        }

        for (Artiste artiste : morceau.getArtistes()) {
            registerExistingArtiste(artiste);
        }
        registerExistingAlbum(morceau.getAlbum());

        String key = buildKey("track", morceau.getSource(), morceau.getSourceId());
        morceauxByKey.putIfAbsent(key, morceau);
        morceauxById.putIfAbsent(morceau.getId(), morceau);
        ajusterProchainId(morceau.getId());
    }

    public synchronized void vider() {
        artistesByKey.clear();
        albumsByKey.clear();
        morceauxByKey.clear();
        artistesById.clear();
        albumsById.clear();
        morceauxById.clear();
        nextRemoteId.set(-1);
    }

    private String buildKey(String type, Source source, String sourceId) {
        return type + "|" + source.name() + "|" + (sourceId == null ? "" : sourceId);
    }

    private void ajusterProchainId(int existingId) {
        if (existingId >= 0) {
            return;
        }
        int prochain = existingId - 1;
        if (prochain < nextRemoteId.get()) {
            nextRemoteId.set(prochain);
        }
    }
}
