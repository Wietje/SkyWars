package vc.pvp.skywars.storage;

import vc.pvp.skywars.player.GamePlayer;

import javax.annotation.Nonnull;

public abstract class DataStorage {

    public abstract void loadPlayer(@Nonnull GamePlayer gamePlayer);

    public abstract void savePlayer(@Nonnull GamePlayer gamePlayer);

    public enum DataStorageType {
        FILE,
        SQL
    }

    private static DataStorage instance;

    public static void setInstance(DataStorageType dataStorageType) {
        switch (dataStorageType) {
            case FILE:
                instance = new FlatFileStorage();
                break;
            case SQL:
                instance = new SQLStorage();
                break;
        }
    }

    public static DataStorage get() {
        if (instance == null) {
            instance = new FlatFileStorage();
        }

        return instance;
    }
}