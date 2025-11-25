package net.justrotem.lobby.sql;

import net.justrotem.data.Main;
import net.justrotem.data.sql.AsyncMySQLManager;
import net.justrotem.data.sql.AsyncUserDataManager;
import net.justrotem.data.sql.MySQL;

public class MySQLConfig implements MySQL {

    private final AsyncMySQLManager sql;
    private final AsyncUserDataManager userData;
    private final AsyncNickDataManager nickData;

    public MySQLConfig() {
        net.justrotem.data.sql.MySQLConfig config = Main.getInstance().getMySQLConfig();

        sql = config.getSQL();
        userData = config.getUserData();
        nickData = new AsyncNickDataManager(sql);
    }

    public AsyncNickDataManager getNickData() {
        return this.nickData;
    }

    @Override
    public void disconnect() {
        sql.disconnect();
    }

    @Override
    public AsyncMySQLManager getSQL() {
        return this.sql;
    }

    @Override
    public AsyncUserDataManager getUserData() {
        return this.userData;
    }
}
