package net.justrotem.data.sql;

public interface MySQL {

    void disconnect();

    AsyncMySQLManager getSQL();

    AsyncUserDataManager getUserData();
}
