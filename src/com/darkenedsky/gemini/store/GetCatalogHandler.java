package com.darkenedsky.gemini.store;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.handler.Handler;
import com.darkenedsky.gemini.service.JDBCConnection;

public class GetCatalogHandler extends Handler {

	@Override
	public void processMessage(Message e, Player p) throws Exception {
		JDBCConnection jdbc = getService().getServer().getJDBC();
		PreparedStatement ps = jdbc.prepareStatement("select * from storeitems join storecatalog on (storeitems.storeitemid = storecatalog.storeitemid) where language = ?;");
		ps.setString(1, p.getLanguage());
		ResultSet rs = null;
		Message m = new Message(STORE_GETCATALOG);
		m.addList("items");
		try {
			rs = ps.executeQuery();
			if (rs.first()) {
				while (true) {
					StoreCatalog sc = new StoreCatalog(rs);
					m.addToList("items", sc, p);
					if (rs.isLast())
						break;
					rs.next();
				}
			}
			rs.close();
			p.pushOutgoingMessage(m);
		} catch (Exception x) {
			if (rs != null)
				rs.close();
			throw x;
		}
	}

}
