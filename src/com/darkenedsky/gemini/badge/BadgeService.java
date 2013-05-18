package com.darkenedsky.gemini.badge;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import com.darkenedsky.gemini.ActionList;
import com.darkenedsky.gemini.LibrarySection;
import com.darkenedsky.gemini.Message;
import com.darkenedsky.gemini.Player;
import com.darkenedsky.gemini.exception.BadgeAlreadyEarnedException;
import com.darkenedsky.gemini.service.JDBCConnection;
import com.darkenedsky.gemini.service.Service;

public class BadgeService extends Service {

	public void grantBadge(Player p, int badgeid) throws Exception {

		JDBCConnection jdbc = getServer().getJDBC();
		Badge b = (Badge) getServer().getLibrary().getSection("badges").get(badgeid);
		for (Badge bb : p.getBadges()) {
			if (bb.getDefinitionID() == badgeid)
				throw new BadgeAlreadyEarnedException(badgeid);
		}
		p.getBadges().add(b);
		PreparedStatement ps = jdbc.prepareStatement("insert into playerbadges (playerid, badgeid) values (?,?);");
		ps.setLong(1, p.getPlayerID());
		ps.setInt(2, badgeid);
		ps.executeUpdate();

		Message m = new Message(ActionList.GRANT_BADGE);
		m.put("badge", b, p);
		p.pushOutgoingMessage(m);
	}

	@Override
	public void init() {
		LibrarySection sect = getServer().getLibrary().addSection("badges");
		sect.add(new DarkenedSkyEmployee());
	}

	public Vector<Badge> loadBadges(long playerid) throws Exception {

		JDBCConnection jdbc = getServer().getJDBC();
		Vector<Badge> bad = new Vector<Badge>();
		ResultSet set = null;
		try {
			PreparedStatement ps = jdbc.prepareStatement("select * from playerbadges where playerid = ?;");
			ps.setLong(1, playerid);
			set = ps.executeQuery();
			if (set.first()) {
				while (true) {
					bad.add((Badge) getServer().getLibrary().getSection("badges").get(set.getInt("badgeid")));
					if (set.isLast())
						break;
					set.next();
				}
			}
			set.close();
			return bad;
		} catch (Exception x) {
			if (set != null)
				set.close();
			throw x;
		}
	}

}
