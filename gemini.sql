PGDMP     -                    q            gemini    9.0.4    9.0.4 �    �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                       false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     )   SET standard_conforming_strings = 'off';
                       false            �           1262    26497    gemini    DATABASE     �   CREATE DATABASE gemini WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'English_United States.1252' LC_CTYPE = 'English_United States.1252';
    DROP DATABASE gemini;
             postgres    false                        2615    2200    public    SCHEMA        CREATE SCHEMA public;
    DROP SCHEMA public;
             postgres    false            �           0    0    SCHEMA public    COMMENT     6   COMMENT ON SCHEMA public IS 'standard public schema';
                  postgres    false    5            �           0    0    public    ACL     �   REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;
                  postgres    false    5            y           2612    11574    plpgsql    PROCEDURAL LANGUAGE     /   CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;
 "   DROP PROCEDURAL LANGUAGE plpgsql;
             postgres    false            "            1255    35316    ccg_clone_deck(bigint, bigint)    FUNCTION     p  CREATE FUNCTION ccg_clone_deck(pplayerid bigint, pdeckid bigint) RETURNS bigint
    LANGUAGE plpgsql
    AS $$

  declare	
	did bigint;
	sid integer;
  begin
	select into did max(deckid) from ccg_decks where (playerid = pplayerid or playerid is null) and deckid = pdeckid;
	select into sid max(serviceid) from ccg_decks where (playerid = pplayerid or playerid is null) and deckid = pdeckid;
	if (did is null) then 
		raise exception 'com.darkenedsky.gemini.exception.CCGInvalidDeckException';
	end if;
	
	insert into ccg_decks (playerid, serviceid, deckname) select pplayerid, serviceid, deckname from ccg_decks where deckid = pdeckid;
	select into did max(deckid) from ccg_decks where playerid = pplayerid and serviceid = sid;

	insert into ccg_deckcards (deckid, definitionid, qty) select did, definitionid, qty from ccg_deckcards where deckid = pdeckid;
	return did;
end;
 $$;
 G   DROP FUNCTION public.ccg_clone_deck(pplayerid bigint, pdeckid bigint);
       public       postgres    false    377    5                        1255    35301 3   ccg_create_deck(bigint, integer, character varying)    FUNCTION     �  CREATE FUNCTION ccg_create_deck(pplayerid bigint, pserviceid integer, pdeckname character varying) RETURNS bigint
    LANGUAGE plpgsql
    AS $$

  declare	
	did bigint;
	
  begin
	select into did max(deckid) from ccg_decks where playerid = pplayerid and serviceid = pserviceid and deckname ilike pdeckname;
	if (did is not null) then 
		raise exception 'com.darkenedsky.gemini.exception.CCGDuplicateDeckNameException';
	end if;

	insert into ccg_decks (playerid, serviceid, deckname) values (pplayerid, pserviceid, pdeckname);
	select into did max(deckid) from ccg_decks where playerid = pplayerid and deckname = pdeckname;
	return did;
end;
 $$;
 i   DROP FUNCTION public.ccg_create_deck(pplayerid bigint, pserviceid integer, pdeckname character varying);
       public       postgres    false    5    377                         1255    35302 (   ccg_delete_deck(bigint, integer, bigint)    FUNCTION       CREATE FUNCTION ccg_delete_deck(pplayerid bigint, pserviceid integer, pdeckid bigint) RETURNS SETOF void
    LANGUAGE plpgsql ROWS 1
    AS $$

  declare
	deckowner bigint;	
	
  begin
	select into deckowner playerid from ccg_decks where deckid = pdeckid;
	if (deckowner is null or deckowner != pplayerid) then 
		raise exception 'com.darkenedsky.gemini.exception.CCGInvalidDeckException';
	end if;
	
	delete from ccg_deckcards where deckid = pdeckid;
	delete from ccg_decks where deckid = pdeckid;
	return;
end;
 $$;
 \   DROP FUNCTION public.ccg_delete_deck(pplayerid bigint, pserviceid integer, pdeckid bigint);
       public       postgres    false    5    377                        1255    34994    ccg_get_sets(bigint)    FUNCTION     +  CREATE FUNCTION ccg_get_sets(pplayerid bigint) RETURNS SETOF integer
    LANGUAGE plpgsql ROWS 10
    AS $$
		
  begin
        create temporary table temp_ccg_get_sets (setid integer not null) on commit drop;
	insert into temp_ccg_get_sets (setid) (select setid from ccgsetsforplayers where playerid = pplayerid);
	insert into temp_ccg_get_sets (setid) (select setid from ccgsetsforsubs where subscriptionid = (select subscriptionid from playeraccounts where playerid = pplayerid));
	return query select distinct setid from temp_ccg_get_sets;
  end;
 $$;
 5   DROP FUNCTION public.ccg_get_sets(pplayerid bigint);
       public       postgres    false    377    5            !            1255    35303 %   ccg_get_usable_cards(bigint, integer)    FUNCTION     �  CREATE FUNCTION ccg_get_usable_cards(pplayerid bigint, pserviceid integer) RETURNS SETOF bigint
    LANGUAGE plpgsql
    AS $$

  begin
	return query select distinct definitionid from card_in_set where 
	(setid in (select setid from card_setsforplayers where playerid = pplayerid) or setid in (select setid from card_setsforsubs where subscriptionid = 
	(select subscriptionid from playeraccounts where playerid = pplayerid))) and setid in (select setid from card_sets where serviceid = pserviceid);
end;
 $$;
 Q   DROP FUNCTION public.ccg_get_usable_cards(pplayerid bigint, pserviceid integer);
       public       postgres    false    377    5                        1255    26710 �   create_account(character varying, character varying, character varying, character varying, boolean, character varying, character varying, integer, character varying)    FUNCTION     F  CREATE FUNCTION create_account(pusername character varying, ppassword character varying, ppassword2 character varying, pemail character varying, pcoppa boolean, pip character varying, pclient character varying, pgender integer, planguage character varying) RETURNS SETOF character varying
    LANGUAGE plpgsql ROWS 1
    AS $$

  declare
	ban_until timestamp;
	ban_reason text;
	player_id bigint;
	gender int;
	token character varying;
	lang character varying;
	
  begin
	
	select into ban_until, ban_reason banneduntil, banreason from bannedip where bannedip.bannedip = pip;
	if (pip is not null and ban_until is not null and ban_until > now()) then
		raise exception 'BannedIP IP Address is banned.';
	end if;

	if (ppassword isnull or ppassword2 isnull or pusername isnull or pemail isnull or pcoppa isnull) then
		raise exception 'Required parameter is null.';
	end if;
	
	if (ppassword != ppassword2) then
		raise exception 'Passwords entered do not match.';
	end if;
	
	select into player_id players.playerid from playeraccounts join players on (players.playerid = playeraccounts.playerid) where username = pusername or email = pemail;
	if (player_id is not null) then
		raise exception 'User already exists with this email or username.';		
	end if;

	-- we are clear to make an account
	lang = planguage;
	if (lang isnull) then
		lang = 'en';
	end if;
	gender = pgender;
	if (gender isnull or gender >= 2 or gender <= -2) then
		gender = 0;
	end if;

	insert into players (username, language, gender) values (pusername, lang, gender);

	select currval('players_playerid_seq') into player_id;
	insert into playeraccounts(playerid, email, password, lastlogintime, lastloginip, lastloginclient, coppa) values (player_id, pemail, encrypt(ppassword), now(), pip, pclient, pcoppa);	
	return query select log_in(pusername, ppassword, pip, pclient);

  end;
 $$;
   DROP FUNCTION public.create_account(pusername character varying, ppassword character varying, ppassword2 character varying, pemail character varying, pcoppa boolean, pip character varying, pclient character varying, pgender integer, planguage character varying);
       public       postgres    false    377    5                        1255    35211    encrypt(character varying)    FUNCTION     �   CREATE FUNCTION encrypt(ppassword character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
  begin
	--TODO make this not suck
	return ppassword;
  end;
 $$;
 ;   DROP FUNCTION public.encrypt(ppassword character varying);
       public       postgres    false    5    377                        1255    26726 :   forgotpass_maketoken(character varying, character varying)    FUNCTION     1  CREATE FUNCTION forgotpass_maketoken(pemail character varying, pip character varying) RETURNS SETOF character varying
    LANGUAGE plpgsql
    AS $$

  declare
	token character varying;	
	player_id bigint;
	ban_until timestamp;
	ban_reason text;
	
  begin
        if (pemail isnull) then
		raise exception 'You must provide an email address to find the account.';
	end if;
	
	select into ban_until, ban_reason banneduntil, banreason from bannedip where bannedip.bannedip = pip;
	if (pip is not null and ban_until is not null and ban_until > now()) then
		raise exception 'BannedIP IP Address is banned.';
	end if;

	select players.playerid into player_id from playeraccounts join players on (players.playerid = playeraccounts.playerid) where email = pemail;

	if (player_id is null) then
		raise exception 'User with this email address not found.';
	end if;
	
	token = maketoken(player_id);
	update playeraccounts set forgotpasstoken = token, forgotpassexpires = now() + interval '3 hours' where playerid = player_id;
	
	-- successful login
	return next token;
  end;
 $$;
 \   DROP FUNCTION public.forgotpass_maketoken(pemail character varying, pip character varying);
       public       postgres    false    377    5                        1255    26719 �   forgotpass_redeem(character varying, character varying, character varying, character varying, character varying, character varying)    FUNCTION       CREATE FUNCTION forgotpass_redeem(pemail character varying, ppassword character varying, ppassword2 character varying, ptoken character varying, pip character varying, pclient character varying) RETURNS SETOF character varying
    LANGUAGE plpgsql
    AS $$

  declare
	player_id bigint;
	user_name character varying;
	ban_until timestamp;
	ban_reason text;
	
  begin
        if (pemail isnull) then
		raise exception 'You must provide an email address to find the account.';
	end if;
	
	if (pip is not null) then 
		select into ban_until, ban_reason banneduntil, banreason from bannedip where bannedip.bannedip = pip;
		if (ban_until is not null and ban_until > now()) then
			raise exception 'BannedIP IP Address is banned.';
		end if;
	end if;

	if (ppassword isnull or ppassword2 isnull or pemail isnull or ptoken isnull) then
		raise exception 'Required parameter is null.';
	end if;

	if (ppassword != ppassword2) then
		raise exception 'Passwords entered do not match.';
	end if;

	if (ppassword ilike pemail) then
		raise exception 'Password may not match username or email address.';
	end if;
	
	perform password_strength_check(ppassword);

	select into player_id, user_name playerid, username from players join playeraccounts on (players.playerid = playeraccounts.playerid) where email = pemail and forgotpasstoken = ptoken and now() < forgotpassexpires;

	if (player_id isnull) then
		raise exception 'Account not found, or token has expired.';	
	end if;
	if (user_name ilike pemail) then
		raise exception 'Password may not match username or email address.';
	end if;
	
	update playeraccounts set forgotpasstoken = null, forgotpassexpires = null, password = encrypt(ppassword) where playerid = player_id;

	return query select log_in(user_name, ppassword, pip, pclient);
  end;
  
 $$;
 �   DROP FUNCTION public.forgotpass_redeem(pemail character varying, ppassword character varying, ppassword2 character varying, ptoken character varying, pip character varying, pclient character varying);
       public       postgres    false    377    5                        1255    35213 R   log_in(character varying, character varying, character varying, character varying)    FUNCTION     �  CREATE FUNCTION log_in(pusername character varying, ppassword character varying, pip character varying, pclient character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$

  declare
	ban_until timestamp;
	ban_reason text;
	player_id bigint;
	badpass int;
	token character varying;
	
  begin
	select into ban_until, ban_reason banneduntil, banreason from bannedip where bannedip.bannedip = pip;
	if (ban_until is not null and ban_until > now()) then
		raise exception 'BannedIP IP Address is banned.';
	end if;

	select into ban_until, ban_reason, player_id banneduntil, banreason, players.playerid from playeraccounts join players on (players.playerid = playeraccounts.playerid) where username ilike pusername and password = encrypt(ppassword);
	if (player_id is null) then

		-- update the bad password attempts
		update playeraccounts set badpassattempts = badpassattempts + 1 where playerid = (select playerid from players where username = pusername);
		select badpassattempts into badpass from playeraccounts where playerid = (select playerid from players where username = pusername);
		if (badpass >= 3) then
			update playeraccounts set banneduntil = now() + interval '1 hour', banreason='Multiple consecutive login attempts with incorrect password' where playerid = (select playerid from players where username ilike pusername);
			insert into bannedip (bannedip, banneduntil, banreason) values (pip, now() + interval '1 hour', 'Multiple consecutive login attempts with incorrect password' );
		end if;
		token = null;
		return token;
		
	end if;
	if (ban_until is not null and ban_until > now()) then
		raise 'BannedUser This user account is banned.';
	end if;

	-- successful login
	select maketoken(player_id) into token;
	update playeraccounts set banneduntil = null, banreason = null, lastlogintime = now(), lastloginip = pip, badpassattempts = 0, lastloginclient = pclient, forgotpasstoken = null, forgotpassexpires = null, sessiontoken = token where playerid = player_id;
	return token;
  end;
 $$;
 �   DROP FUNCTION public.log_in(pusername character varying, ppassword character varying, pip character varying, pclient character varying);
       public       postgres    false    5    377                        1255    26695    maketoken(bigint)    FUNCTION     �  CREATE FUNCTION maketoken(player_id bigint) RETURNS SETOF character varying
    LANGUAGE plpgsql
    AS $$

  declare
	token character varying;
	token1 bigint;
	token2 bigint;
	token3 bigint;
	
  begin
	token1 = player_id * 1000000;
	select trunc(extract(epoch from now())) into token2;
	select trunc(random() * 99999999 + 10000000) into token3;
	token = to_hex(token1) || to_hex(token2) || to_hex(player_id) || to_hex(token3);

	-- successful login
	return next token;
  end;
 $$;
 2   DROP FUNCTION public.maketoken(player_id bigint);
       public       postgres    false    5    377                        1255    26711 *   password_strength_check(character varying)    FUNCTION     �   CREATE FUNCTION password_strength_check(ppassword character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$

  declare
	
	
  begin
	if (char_length(ppassword) < 6) then
		raise exception 'Password is too short.';
	end if;
		
  end;
 $$;
 K   DROP FUNCTION public.password_strength_check(ppassword character varying);
       public       postgres    false    5    377            0           1259    26742    winlossrecords    TABLE       CREATE TABLE winlossrecords (
    winlossrecordid integer NOT NULL,
    playerid bigint NOT NULL,
    serviceid integer NOT NULL,
    wins integer DEFAULT 0 NOT NULL,
    losses integer DEFAULT 0 NOT NULL,
    draws integer DEFAULT 0 NOT NULL,
    rating integer DEFAULT 0 NOT NULL
);
 "   DROP TABLE public.winlossrecords;
       public         postgres    false    1890    1891    1892    1893    5                        1255    26763 =   set_winlossrecord(bigint, integer, integer, integer, integer)    FUNCTION     ,  CREATE FUNCTION set_winlossrecord(pplayerid bigint, pserviceid integer, pwins integer, plosses integer, pdraws integer) RETURNS SETOF winlossrecords
    LANGUAGE plpgsql ROWS 1
    AS $$
  declare
	newrating integer;
  begin
	newrating = 0;
	-- put code to do rating here.
	
	update winlossrecords set wins = wins + pwins, losses = losses+plosses, draws = draws+pdraws, rating = newrating where playerid = pplayerid and serviceid = pserviceid;
	return query select * from winlossrecords where serviceid = pserviceid and playerid = pplayerid;
	
  end;
 $$;
 ~   DROP FUNCTION public.set_winlossrecord(pplayerid bigint, pserviceid integer, pwins integer, plosses integer, pdraws integer);
       public       postgres    false    351    377    5                        1255    26727 ;   verifyemail_maketoken(character varying, character varying)    FUNCTION     �  CREATE FUNCTION verifyemail_maketoken(pemail character varying, pip character varying) RETURNS SETOF character varying
    LANGUAGE plpgsql
    AS $$

  declare
	ban_until timestamp;
	ban_reason text;
	token character varying;
	player_id bigint;
  begin
	
	select into ban_until, ban_reason banneduntil, banreason from bannedip where bannedip.bannedip = pip;
	if (pip is not null and ban_until is not null and ban_until > now()) then
		raise exception 'BannedIP IP Address is banned.';
	end if;

	select into player_id players.playerid from playeraccounts join players on (players.playerid = playeraccounts.playerid) where email = pemail;
	if (player_id isnull) then
		raise exception 'User with this email address not found.';
	end if;

	select maketoken(player_id) into token;
	update playeraccounts set verifyemailtoken = token, verifyemailexpires = now() + interval '1 day' where playerid = player_id;
	return next token;
  end;
 $$;
 ]   DROP FUNCTION public.verifyemail_maketoken(pemail character varying, pip character varying);
       public       postgres    false    377    5                        1255    26728 K   verifyemail_redeem(character varying, character varying, character varying)    FUNCTION     #  CREATE FUNCTION verifyemail_redeem(pemail character varying, ptoken character varying, pip character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$

  declare
	player_id bigint;
	ban_until timestamp;
	ban_reason text;
	
  begin
        if (pemail isnull) then
		raise exception 'You must provide an email address to find the account.';
	end if;
	
	if (pip is not null) then 
		select into ban_until, ban_reason banneduntil, banreason from bannedip where bannedip.bannedip = pip;
		if (ban_until is not null and ban_until > now()) then
			raise exception 'BannedIP IP Address is banned.';
		end if;
	end if;

	
	select into player_id playerid from players join playeraccounts on (players.playerid = playeraccounts.playerid) where email = pemail and verifyemailtoken = ptoken and now() < verifyemailexpires;

	if (player_id isnull) then
		raise exception 'Account not found, or token has expired.';	
	end if;

	update playeraccounts set verifiedemail = now(), verifyemailtoken = null, verifyemailexpires = null where playerid = player_id;

  end;
  
 $$;
 t   DROP FUNCTION public.verifyemail_redeem(pemail character varying, ptoken character varying, pip character varying);
       public       postgres    false    377    5            &           1259    26610    bannedip    TABLE     �   CREATE TABLE bannedip (
    bannedip character varying NOT NULL,
    banreason character varying NOT NULL,
    banneduntil timestamp without time zone,
    bannedipid integer NOT NULL
);
    DROP TABLE public.bannedip;
       public         postgres    false    5            ,           1259    26697    bannedip_bannedipid_seq    SEQUENCE     y   CREATE SEQUENCE bannedip_bannedipid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 .   DROP SEQUENCE public.bannedip_bannedipid_seq;
       public       postgres    false    5    1574            �           0    0    bannedip_bannedipid_seq    SEQUENCE OWNED BY     E   ALTER SEQUENCE bannedip_bannedipid_seq OWNED BY bannedip.bannedipid;
            public       postgres    false    1580            �           0    0    bannedip_bannedipid_seq    SEQUENCE SET     >   SELECT pg_catalog.setval('bannedip_bannedipid_seq', 1, true);
            public       postgres    false    1580            <           1259    35259    card_in_set    TABLE     }   CREATE TABLE card_in_set (
    definitionid integer NOT NULL,
    setid integer NOT NULL,
    cardinsetid bigint NOT NULL
);
    DROP TABLE public.card_in_set;
       public         postgres    false    5            =           1259    35304    card_in_set_cardinsetid_seq    SEQUENCE     }   CREATE SEQUENCE card_in_set_cardinsetid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 2   DROP SEQUENCE public.card_in_set_cardinsetid_seq;
       public       postgres    false    5    1596            �           0    0    card_in_set_cardinsetid_seq    SEQUENCE OWNED BY     M   ALTER SEQUENCE card_in_set_cardinsetid_seq OWNED BY card_in_set.cardinsetid;
            public       postgres    false    1597            �           0    0    card_in_set_cardinsetid_seq    SEQUENCE SET     C   SELECT pg_catalog.setval('card_in_set_cardinsetid_seq', 13, true);
            public       postgres    false    1597            :           1259    35242 	   card_sets    TABLE     �   CREATE TABLE card_sets (
    setid integer NOT NULL,
    englishname character varying NOT NULL,
    promo boolean DEFAULT false NOT NULL,
    serviceid integer,
    javaclass character varying NOT NULL
);
    DROP TABLE public.card_sets;
       public         postgres    false    1899    5            9           1259    35240    card_sets_setid_seq    SEQUENCE     u   CREATE SEQUENCE card_sets_setid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.card_sets_setid_seq;
       public       postgres    false    1594    5            �           0    0    card_sets_setid_seq    SEQUENCE OWNED BY     =   ALTER SEQUENCE card_sets_setid_seq OWNED BY card_sets.setid;
            public       postgres    false    1593            �           0    0    card_sets_setid_seq    SEQUENCE SET     ;   SELECT pg_catalog.setval('card_sets_setid_seq', 1, false);
            public       postgres    false    1593            6           1259    34963    card_setsforplayers    TABLE     �   CREATE TABLE card_setsforplayers (
    setforplayerid bigint NOT NULL,
    playerid bigint,
    availableuntil timestamp without time zone,
    setid integer NOT NULL
);
 '   DROP TABLE public.card_setsforplayers;
       public         postgres    false    5            8           1259    34976    card_setsforsubs    TABLE     �   CREATE TABLE card_setsforsubs (
    setforsubid integer NOT NULL,
    subscriptionid integer NOT NULL,
    setid integer NOT NULL
);
 $   DROP TABLE public.card_setsforsubs;
       public         postgres    false    5            ;           1259    35257    cards_in_set_cardid_seq    SEQUENCE     y   CREATE SEQUENCE cards_in_set_cardid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 .   DROP SEQUENCE public.cards_in_set_cardid_seq;
       public       postgres    false    1596    5            �           0    0    cards_in_set_cardid_seq    SEQUENCE OWNED BY     J   ALTER SEQUENCE cards_in_set_cardid_seq OWNED BY card_in_set.definitionid;
            public       postgres    false    1595            �           0    0    cards_in_set_cardid_seq    SEQUENCE SET     ?   SELECT pg_catalog.setval('cards_in_set_cardid_seq', 1, false);
            public       postgres    false    1595            1           1259    34921    ccg_deckcards    TABLE     �   CREATE TABLE ccg_deckcards (
    cardindeckid bigint NOT NULL,
    deckid bigint NOT NULL,
    definitionid bigint NOT NULL,
    qty integer NOT NULL
);
 !   DROP TABLE public.ccg_deckcards;
       public         postgres    false    5            3           1259    34926 	   ccg_decks    TABLE     �   CREATE TABLE ccg_decks (
    deckid bigint NOT NULL,
    deckname character varying NOT NULL,
    playerid bigint,
    serviceid integer NOT NULL
);
    DROP TABLE public.ccg_decks;
       public         postgres    false    5            4           1259    34940     ccgcardsindecks_cardindeckid_seq    SEQUENCE     �   CREATE SEQUENCE ccgcardsindecks_cardindeckid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 7   DROP SEQUENCE public.ccgcardsindecks_cardindeckid_seq;
       public       postgres    false    1585    5            �           0    0     ccgcardsindecks_cardindeckid_seq    SEQUENCE OWNED BY     U   ALTER SEQUENCE ccgcardsindecks_cardindeckid_seq OWNED BY ccg_deckcards.cardindeckid;
            public       postgres    false    1588            �           0    0     ccgcardsindecks_cardindeckid_seq    SEQUENCE SET     G   SELECT pg_catalog.setval('ccgcardsindecks_cardindeckid_seq', 6, true);
            public       postgres    false    1588            2           1259    34924    ccgdecks_deckid_seq    SEQUENCE     u   CREATE SEQUENCE ccgdecks_deckid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.ccgdecks_deckid_seq;
       public       postgres    false    1587    5            �           0    0    ccgdecks_deckid_seq    SEQUENCE OWNED BY     >   ALTER SEQUENCE ccgdecks_deckid_seq OWNED BY ccg_decks.deckid;
            public       postgres    false    1586            �           0    0    ccgdecks_deckid_seq    SEQUENCE SET     :   SELECT pg_catalog.setval('ccgdecks_deckid_seq', 7, true);
            public       postgres    false    1586            5           1259    34961 $   ccgsetsforplayers_setforplayerid_seq    SEQUENCE     �   CREATE SEQUENCE ccgsetsforplayers_setforplayerid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 ;   DROP SEQUENCE public.ccgsetsforplayers_setforplayerid_seq;
       public       postgres    false    1590    5            �           0    0 $   ccgsetsforplayers_setforplayerid_seq    SEQUENCE OWNED BY     a   ALTER SEQUENCE ccgsetsforplayers_setforplayerid_seq OWNED BY card_setsforplayers.setforplayerid;
            public       postgres    false    1589            �           0    0 $   ccgsetsforplayers_setforplayerid_seq    SEQUENCE SET     K   SELECT pg_catalog.setval('ccgsetsforplayers_setforplayerid_seq', 6, true);
            public       postgres    false    1589            7           1259    34974    ccgsetsforsubs_setforsubid_seq    SEQUENCE     �   CREATE SEQUENCE ccgsetsforsubs_setforsubid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 5   DROP SEQUENCE public.ccgsetsforsubs_setforsubid_seq;
       public       postgres    false    5    1592            �           0    0    ccgsetsforsubs_setforsubid_seq    SEQUENCE OWNED BY     U   ALTER SEQUENCE ccgsetsforsubs_setforsubid_seq OWNED BY card_setsforsubs.setforsubid;
            public       postgres    false    1591            �           0    0    ccgsetsforsubs_setforsubid_seq    SEQUENCE SET     E   SELECT pg_catalog.setval('ccgsetsforsubs_setforsubid_seq', 2, true);
            public       postgres    false    1591            (           1259    26620    playeraccounts    TABLE     �  CREATE TABLE playeraccounts (
    accountid bigint NOT NULL,
    playerid bigint NOT NULL,
    email character varying NOT NULL,
    subscribersince timestamp without time zone,
    createdon timestamp without time zone DEFAULT now() NOT NULL,
    subscriptionexpires timestamp without time zone,
    subscriptionid integer DEFAULT 1 NOT NULL,
    lastloginip character varying,
    lastlogintime timestamp without time zone,
    sessiontoken character varying,
    password character varying NOT NULL,
    lastloginclient character varying,
    forgotpasstoken character varying,
    forgotpassexpires timestamp without time zone,
    banneduntil timestamp without time zone,
    banreason character varying,
    badpassattempts integer DEFAULT 0 NOT NULL,
    verifyemailtoken character varying,
    verifyemailexpires timestamp without time zone,
    coppa boolean,
    verifiedemail timestamp without time zone
);
 "   DROP TABLE public.playeraccounts;
       public         postgres    false    1881    1882    1883    5            '           1259    26618    playeraccounts_accountid_seq    SEQUENCE     ~   CREATE SEQUENCE playeraccounts_accountid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 3   DROP SEQUENCE public.playeraccounts_accountid_seq;
       public       postgres    false    5    1576            �           0    0    playeraccounts_accountid_seq    SEQUENCE OWNED BY     O   ALTER SEQUENCE playeraccounts_accountid_seq OWNED BY playeraccounts.accountid;
            public       postgres    false    1575            �           0    0    playeraccounts_accountid_seq    SEQUENCE SET     C   SELECT pg_catalog.setval('playeraccounts_accountid_seq', 8, true);
            public       postgres    false    1575            "           1259    26500    players    TABLE     �   CREATE TABLE players (
    playerid bigint NOT NULL,
    username character varying NOT NULL,
    language character varying DEFAULT 'en'::character varying NOT NULL,
    gender integer DEFAULT 1 NOT NULL
);
    DROP TABLE public.players;
       public         postgres    false    1876    1877    5            !           1259    26498    players_playerid_seq    SEQUENCE     v   CREATE SEQUENCE players_playerid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 +   DROP SEQUENCE public.players_playerid_seq;
       public       postgres    false    1570    5            �           0    0    players_playerid_seq    SEQUENCE OWNED BY     ?   ALTER SEQUENCE players_playerid_seq OWNED BY players.playerid;
            public       postgres    false    1569            �           0    0    players_playerid_seq    SEQUENCE SET     ;   SELECT pg_catalog.setval('players_playerid_seq', 6, true);
            public       postgres    false    1569            %           1259    26580 
   promocodes    TABLE     �   CREATE TABLE promocodes (
    promoid integer NOT NULL,
    promocode character varying NOT NULL,
    expirationdate timestamp without time zone,
    usesallowed integer,
    storeitemid integer NOT NULL
);
    DROP TABLE public.promocodes;
       public         postgres    false    5            .           1259    26731    services    TABLE     f   CREATE TABLE services (
    serviceid integer NOT NULL,
    servicename character varying NOT NULL
);
    DROP TABLE public.services;
       public         postgres    false    5            -           1259    26729    services_serviceid_seq    SEQUENCE     x   CREATE SEQUENCE services_serviceid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.services_serviceid_seq;
       public       postgres    false    1582    5            �           0    0    services_serviceid_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE services_serviceid_seq OWNED BY services.serviceid;
            public       postgres    false    1581            �           0    0    services_serviceid_seq    SEQUENCE SET     >   SELECT pg_catalog.setval('services_serviceid_seq', 1, false);
            public       postgres    false    1581            )           1259    26641 
   storeitems    TABLE     ;  CREATE TABLE storeitems (
    storeitemid integer NOT NULL,
    priceusd money NOT NULL,
    availablestart timestamp without time zone DEFAULT now() NOT NULL,
    availableend timestamp without time zone,
    stocklimit integer,
    detailsurl character varying,
    show_in_store boolean DEFAULT true NOT NULL
);
    DROP TABLE public.storeitems;
       public         postgres    false    1884    1885    5            +           1259    26652    storepurchases    TABLE       CREATE TABLE storepurchases (
    purchaseid bigint NOT NULL,
    storeitemid integer NOT NULL,
    playerid bigint NOT NULL,
    transactionid bigint,
    purchasedon timestamp without time zone DEFAULT now() NOT NULL,
    purchasedfromip character varying,
    promoid integer
);
 "   DROP TABLE public.storepurchases;
       public         postgres    false    1887    5            *           1259    26650    storepurchases_purchaseid_seq    SEQUENCE        CREATE SEQUENCE storepurchases_purchaseid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 4   DROP SEQUENCE public.storepurchases_purchaseid_seq;
       public       postgres    false    5    1579            �           0    0    storepurchases_purchaseid_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE storepurchases_purchaseid_seq OWNED BY storepurchases.purchaseid;
            public       postgres    false    1578            �           0    0    storepurchases_purchaseid_seq    SEQUENCE SET     E   SELECT pg_catalog.setval('storepurchases_purchaseid_seq', 1, false);
            public       postgres    false    1578            $           1259    26558    subscriptions    TABLE     D   CREATE TABLE subscriptions (
    subscriptionid integer NOT NULL
);
 !   DROP TABLE public.subscriptions;
       public         postgres    false    5            #           1259    26556     subscriptions_subscriptionid_seq    SEQUENCE     �   CREATE SEQUENCE subscriptions_subscriptionid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 7   DROP SEQUENCE public.subscriptions_subscriptionid_seq;
       public       postgres    false    5    1572            �           0    0     subscriptions_subscriptionid_seq    SEQUENCE OWNED BY     W   ALTER SEQUENCE subscriptions_subscriptionid_seq OWNED BY subscriptions.subscriptionid;
            public       postgres    false    1571            �           0    0     subscriptions_subscriptionid_seq    SEQUENCE SET     H   SELECT pg_catalog.setval('subscriptions_subscriptionid_seq', 1, false);
            public       postgres    false    1571            /           1259    26740 "   winlossrecords_winlossrecordid_seq    SEQUENCE     �   CREATE SEQUENCE winlossrecords_winlossrecordid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 9   DROP SEQUENCE public.winlossrecords_winlossrecordid_seq;
       public       postgres    false    5    1584            �           0    0 "   winlossrecords_winlossrecordid_seq    SEQUENCE OWNED BY     [   ALTER SEQUENCE winlossrecords_winlossrecordid_seq OWNED BY winlossrecords.winlossrecordid;
            public       postgres    false    1583            �           0    0 "   winlossrecords_winlossrecordid_seq    SEQUENCE SET     J   SELECT pg_catalog.setval('winlossrecords_winlossrecordid_seq', 1, false);
            public       postgres    false    1583            W           2604    26699 
   bannedipid    DEFAULT     g   ALTER TABLE bannedip ALTER COLUMN bannedipid SET DEFAULT nextval('bannedip_bannedipid_seq'::regclass);
 B   ALTER TABLE public.bannedip ALTER COLUMN bannedipid DROP DEFAULT;
       public       postgres    false    1580    1574            l           2604    35306    cardinsetid    DEFAULT     o   ALTER TABLE card_in_set ALTER COLUMN cardinsetid SET DEFAULT nextval('card_in_set_cardinsetid_seq'::regclass);
 F   ALTER TABLE public.card_in_set ALTER COLUMN cardinsetid DROP DEFAULT;
       public       postgres    false    1597    1596            j           2604    35245    setid    DEFAULT     _   ALTER TABLE card_sets ALTER COLUMN setid SET DEFAULT nextval('card_sets_setid_seq'::regclass);
 >   ALTER TABLE public.card_sets ALTER COLUMN setid DROP DEFAULT;
       public       postgres    false    1594    1593    1594            h           2604    34966    setforplayerid    DEFAULT     �   ALTER TABLE card_setsforplayers ALTER COLUMN setforplayerid SET DEFAULT nextval('ccgsetsforplayers_setforplayerid_seq'::regclass);
 Q   ALTER TABLE public.card_setsforplayers ALTER COLUMN setforplayerid DROP DEFAULT;
       public       postgres    false    1589    1590    1590            i           2604    34979    setforsubid    DEFAULT     w   ALTER TABLE card_setsforsubs ALTER COLUMN setforsubid SET DEFAULT nextval('ccgsetsforsubs_setforsubid_seq'::regclass);
 K   ALTER TABLE public.card_setsforsubs ALTER COLUMN setforsubid DROP DEFAULT;
       public       postgres    false    1591    1592    1592            f           2604    34942    cardindeckid    DEFAULT     w   ALTER TABLE ccg_deckcards ALTER COLUMN cardindeckid SET DEFAULT nextval('ccgcardsindecks_cardindeckid_seq'::regclass);
 I   ALTER TABLE public.ccg_deckcards ALTER COLUMN cardindeckid DROP DEFAULT;
       public       postgres    false    1588    1585            g           2604    34929    deckid    DEFAULT     `   ALTER TABLE ccg_decks ALTER COLUMN deckid SET DEFAULT nextval('ccgdecks_deckid_seq'::regclass);
 ?   ALTER TABLE public.ccg_decks ALTER COLUMN deckid DROP DEFAULT;
       public       postgres    false    1587    1586    1587            X           2604    26623 	   accountid    DEFAULT     q   ALTER TABLE playeraccounts ALTER COLUMN accountid SET DEFAULT nextval('playeraccounts_accountid_seq'::regclass);
 G   ALTER TABLE public.playeraccounts ALTER COLUMN accountid DROP DEFAULT;
       public       postgres    false    1576    1575    1576            S           2604    26503    playerid    DEFAULT     a   ALTER TABLE players ALTER COLUMN playerid SET DEFAULT nextval('players_playerid_seq'::regclass);
 ?   ALTER TABLE public.players ALTER COLUMN playerid DROP DEFAULT;
       public       postgres    false    1570    1569    1570            `           2604    26734 	   serviceid    DEFAULT     e   ALTER TABLE services ALTER COLUMN serviceid SET DEFAULT nextval('services_serviceid_seq'::regclass);
 A   ALTER TABLE public.services ALTER COLUMN serviceid DROP DEFAULT;
       public       postgres    false    1582    1581    1582            ^           2604    26655 
   purchaseid    DEFAULT     s   ALTER TABLE storepurchases ALTER COLUMN purchaseid SET DEFAULT nextval('storepurchases_purchaseid_seq'::regclass);
 H   ALTER TABLE public.storepurchases ALTER COLUMN purchaseid DROP DEFAULT;
       public       postgres    false    1578    1579    1579            V           2604    26561    subscriptionid    DEFAULT     y   ALTER TABLE subscriptions ALTER COLUMN subscriptionid SET DEFAULT nextval('subscriptions_subscriptionid_seq'::regclass);
 K   ALTER TABLE public.subscriptions ALTER COLUMN subscriptionid DROP DEFAULT;
       public       postgres    false    1572    1571    1572            a           2604    26745    winlossrecordid    DEFAULT     }   ALTER TABLE winlossrecords ALTER COLUMN winlossrecordid SET DEFAULT nextval('winlossrecords_winlossrecordid_seq'::regclass);
 M   ALTER TABLE public.winlossrecords ALTER COLUMN winlossrecordid DROP DEFAULT;
       public       postgres    false    1583    1584    1584            �          0    26610    bannedip 
   TABLE DATA               I   COPY bannedip (bannedip, banreason, banneduntil, bannedipid) FROM stdin;
    public       postgres    false    1574   ��       �          0    35259    card_in_set 
   TABLE DATA               @   COPY card_in_set (definitionid, setid, cardinsetid) FROM stdin;
    public       postgres    false    1596   ��       �          0    35242 	   card_sets 
   TABLE DATA               M   COPY card_sets (setid, englishname, promo, serviceid, javaclass) FROM stdin;
    public       postgres    false    1594   ��       �          0    34963    card_setsforplayers 
   TABLE DATA               W   COPY card_setsforplayers (setforplayerid, playerid, availableuntil, setid) FROM stdin;
    public       postgres    false    1590   ;�       �          0    34976    card_setsforsubs 
   TABLE DATA               G   COPY card_setsforsubs (setforsubid, subscriptionid, setid) FROM stdin;
    public       postgres    false    1592   X�       �          0    34921    ccg_deckcards 
   TABLE DATA               I   COPY ccg_deckcards (cardindeckid, deckid, definitionid, qty) FROM stdin;
    public       postgres    false    1585   ��       �          0    34926 	   ccg_decks 
   TABLE DATA               C   COPY ccg_decks (deckid, deckname, playerid, serviceid) FROM stdin;
    public       postgres    false    1587   ��       �          0    26620    playeraccounts 
   TABLE DATA               L  COPY playeraccounts (accountid, playerid, email, subscribersince, createdon, subscriptionexpires, subscriptionid, lastloginip, lastlogintime, sessiontoken, password, lastloginclient, forgotpasstoken, forgotpassexpires, banneduntil, banreason, badpassattempts, verifyemailtoken, verifyemailexpires, coppa, verifiedemail) FROM stdin;
    public       postgres    false    1576   
�       �          0    26500    players 
   TABLE DATA               @   COPY players (playerid, username, language, gender) FROM stdin;
    public       postgres    false    1570   ��       �          0    26580 
   promocodes 
   TABLE DATA               [   COPY promocodes (promoid, promocode, expirationdate, usesallowed, storeitemid) FROM stdin;
    public       postgres    false    1573   �       �          0    26731    services 
   TABLE DATA               3   COPY services (serviceid, servicename) FROM stdin;
    public       postgres    false    1582   )�       �          0    26641 
   storeitems 
   TABLE DATA               y   COPY storeitems (storeitemid, priceusd, availablestart, availableend, stocklimit, detailsurl, show_in_store) FROM stdin;
    public       postgres    false    1577   f�       �          0    26652    storepurchases 
   TABLE DATA               z   COPY storepurchases (purchaseid, storeitemid, playerid, transactionid, purchasedon, purchasedfromip, promoid) FROM stdin;
    public       postgres    false    1579   ��       �          0    26558    subscriptions 
   TABLE DATA               0   COPY subscriptions (subscriptionid) FROM stdin;
    public       postgres    false    1572   ��       �          0    26742    winlossrecords 
   TABLE DATA               d   COPY winlossrecords (winlossrecordid, playerid, serviceid, wins, losses, draws, rating) FROM stdin;
    public       postgres    false    1584   ��       �           2606    35314    card_in_set_pkey 
   CONSTRAINT     \   ALTER TABLE ONLY card_in_set
    ADD CONSTRAINT card_in_set_pkey PRIMARY KEY (cardinsetid);
 F   ALTER TABLE ONLY public.card_in_set DROP CONSTRAINT card_in_set_pkey;
       public         postgres    false    1596    1596            �           2606    35251    card_sets_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY card_sets
    ADD CONSTRAINT card_sets_pkey PRIMARY KEY (setid);
 B   ALTER TABLE ONLY public.card_sets DROP CONSTRAINT card_sets_pkey;
       public         postgres    false    1594    1594            ~           2606    34947    ccgcardsindecks_pkey 
   CONSTRAINT     c   ALTER TABLE ONLY ccg_deckcards
    ADD CONSTRAINT ccgcardsindecks_pkey PRIMARY KEY (cardindeckid);
 L   ALTER TABLE ONLY public.ccg_deckcards DROP CONSTRAINT ccgcardsindecks_pkey;
       public         postgres    false    1585    1585            �           2606    34934    ccgdecks_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY ccg_decks
    ADD CONSTRAINT ccgdecks_pkey PRIMARY KEY (deckid);
 A   ALTER TABLE ONLY public.ccg_decks DROP CONSTRAINT ccgdecks_pkey;
       public         postgres    false    1587    1587            �           2606    34968    ccgsetsforplayers_pkey 
   CONSTRAINT     m   ALTER TABLE ONLY card_setsforplayers
    ADD CONSTRAINT ccgsetsforplayers_pkey PRIMARY KEY (setforplayerid);
 T   ALTER TABLE ONLY public.card_setsforplayers DROP CONSTRAINT ccgsetsforplayers_pkey;
       public         postgres    false    1590    1590            �           2606    34984    ccgsetsforsubs_pkey 
   CONSTRAINT     d   ALTER TABLE ONLY card_setsforsubs
    ADD CONSTRAINT ccgsetsforsubs_pkey PRIMARY KEY (setforsubid);
 N   ALTER TABLE ONLY public.card_setsforsubs DROP CONSTRAINT ccgsetsforsubs_pkey;
       public         postgres    false    1592    1592            t           2606    26630    playeraccounts_pkey 
   CONSTRAINT     `   ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_pkey PRIMARY KEY (accountid);
 L   ALTER TABLE ONLY public.playeraccounts DROP CONSTRAINT playeraccounts_pkey;
       public         postgres    false    1576    1576            n           2606    26509    players_pkey 
   CONSTRAINT     Q   ALTER TABLE ONLY players
    ADD CONSTRAINT players_pkey PRIMARY KEY (playerid);
 >   ALTER TABLE ONLY public.players DROP CONSTRAINT players_pkey;
       public         postgres    false    1570    1570            r           2606    26587    promocodes_pkey 
   CONSTRAINT     V   ALTER TABLE ONLY promocodes
    ADD CONSTRAINT promocodes_pkey PRIMARY KEY (promoid);
 D   ALTER TABLE ONLY public.promocodes DROP CONSTRAINT promocodes_pkey;
       public         postgres    false    1573    1573            z           2606    26739    services_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY services
    ADD CONSTRAINT services_pkey PRIMARY KEY (serviceid);
 @   ALTER TABLE ONLY public.services DROP CONSTRAINT services_pkey;
       public         postgres    false    1582    1582            v           2606    26649    storeitems_pkey 
   CONSTRAINT     Z   ALTER TABLE ONLY storeitems
    ADD CONSTRAINT storeitems_pkey PRIMARY KEY (storeitemid);
 D   ALTER TABLE ONLY public.storeitems DROP CONSTRAINT storeitems_pkey;
       public         postgres    false    1577    1577            x           2606    26661    storepurchases_pkey 
   CONSTRAINT     a   ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_pkey PRIMARY KEY (purchaseid);
 L   ALTER TABLE ONLY public.storepurchases DROP CONSTRAINT storepurchases_pkey;
       public         postgres    false    1579    1579            p           2606    26563    subscriptions_pkey 
   CONSTRAINT     c   ALTER TABLE ONLY subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (subscriptionid);
 J   ALTER TABLE ONLY public.subscriptions DROP CONSTRAINT subscriptions_pkey;
       public         postgres    false    1572    1572            |           2606    26751    winlossrecords_pkey 
   CONSTRAINT     f   ALTER TABLE ONLY winlossrecords
    ADD CONSTRAINT winlossrecords_pkey PRIMARY KEY (winlossrecordid);
 L   ALTER TABLE ONLY public.winlossrecords DROP CONSTRAINT winlossrecords_pkey;
       public         postgres    false    1584    1584            �           2606    35252    card_sets_serviceid_fkey    FK CONSTRAINT        ALTER TABLE ONLY card_sets
    ADD CONSTRAINT card_sets_serviceid_fkey FOREIGN KEY (serviceid) REFERENCES services(serviceid);
 L   ALTER TABLE ONLY public.card_sets DROP CONSTRAINT card_sets_serviceid_fkey;
       public       postgres    false    1582    1594    1913            �           2606    35273    card_setsforplayers_setid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY card_setsforplayers
    ADD CONSTRAINT card_setsforplayers_setid_fkey FOREIGN KEY (setid) REFERENCES card_sets(setid);
 \   ALTER TABLE ONLY public.card_setsforplayers DROP CONSTRAINT card_setsforplayers_setid_fkey;
       public       postgres    false    1590    1594    1925            �           2606    35278    card_setsforsubs_setid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY card_setsforsubs
    ADD CONSTRAINT card_setsforsubs_setid_fkey FOREIGN KEY (setid) REFERENCES card_sets(setid);
 V   ALTER TABLE ONLY public.card_setsforsubs DROP CONSTRAINT card_setsforsubs_setid_fkey;
       public       postgres    false    1594    1592    1925            �           2606    35268    cards_in_set_setid_fkey    FK CONSTRAINT     y   ALTER TABLE ONLY card_in_set
    ADD CONSTRAINT cards_in_set_setid_fkey FOREIGN KEY (setid) REFERENCES card_sets(setid);
 M   ALTER TABLE ONLY public.card_in_set DROP CONSTRAINT cards_in_set_setid_fkey;
       public       postgres    false    1925    1596    1594            �           2606    35235    ccg_decks_serviceid_fkey    FK CONSTRAINT        ALTER TABLE ONLY ccg_decks
    ADD CONSTRAINT ccg_decks_serviceid_fkey FOREIGN KEY (serviceid) REFERENCES services(serviceid);
 L   ALTER TABLE ONLY public.ccg_decks DROP CONSTRAINT ccg_decks_serviceid_fkey;
       public       postgres    false    1913    1587    1582            �           2606    34948    ccgcardsindecks_deckid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY ccg_deckcards
    ADD CONSTRAINT ccgcardsindecks_deckid_fkey FOREIGN KEY (deckid) REFERENCES ccg_decks(deckid);
 S   ALTER TABLE ONLY public.ccg_deckcards DROP CONSTRAINT ccgcardsindecks_deckid_fkey;
       public       postgres    false    1587    1585    1919            �           2606    34935    ccgdecks_playerid_fkey    FK CONSTRAINT     z   ALTER TABLE ONLY ccg_decks
    ADD CONSTRAINT ccgdecks_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);
 J   ALTER TABLE ONLY public.ccg_decks DROP CONSTRAINT ccgdecks_playerid_fkey;
       public       postgres    false    1570    1587    1901            �           2606    34969    ccgsetsforplayers_playerid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY card_setsforplayers
    ADD CONSTRAINT ccgsetsforplayers_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);
 ]   ALTER TABLE ONLY public.card_setsforplayers DROP CONSTRAINT ccgsetsforplayers_playerid_fkey;
       public       postgres    false    1570    1590    1901            �           2606    34985 "   ccgsetsforsubs_subscriptionid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY card_setsforsubs
    ADD CONSTRAINT ccgsetsforsubs_subscriptionid_fkey FOREIGN KEY (subscriptionid) REFERENCES subscriptions(subscriptionid);
 ]   ALTER TABLE ONLY public.card_setsforsubs DROP CONSTRAINT ccgsetsforsubs_subscriptionid_fkey;
       public       postgres    false    1592    1903    1572            �           2606    26631    playeraccounts_playerid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);
 U   ALTER TABLE ONLY public.playeraccounts DROP CONSTRAINT playeraccounts_playerid_fkey;
       public       postgres    false    1576    1570    1901            �           2606    26636 "   playeraccounts_subscriptionid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_subscriptionid_fkey FOREIGN KEY (subscriptionid) REFERENCES subscriptions(subscriptionid);
 [   ALTER TABLE ONLY public.playeraccounts DROP CONSTRAINT playeraccounts_subscriptionid_fkey;
       public       postgres    false    1903    1576    1572            �           2606    35291    promocodes_storeitemid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY promocodes
    ADD CONSTRAINT promocodes_storeitemid_fkey FOREIGN KEY (storeitemid) REFERENCES storeitems(storeitemid);
 P   ALTER TABLE ONLY public.promocodes DROP CONSTRAINT promocodes_storeitemid_fkey;
       public       postgres    false    1573    1909    1577            �           2606    26667    storepurchases_playerid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);
 U   ALTER TABLE ONLY public.storepurchases DROP CONSTRAINT storepurchases_playerid_fkey;
       public       postgres    false    1570    1579    1901            �           2606    35296    storepurchases_promoid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_promoid_fkey FOREIGN KEY (promoid) REFERENCES promocodes(promoid);
 T   ALTER TABLE ONLY public.storepurchases DROP CONSTRAINT storepurchases_promoid_fkey;
       public       postgres    false    1573    1579    1905            �           2606    26662    storepurchases_storeitemid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_storeitemid_fkey FOREIGN KEY (storeitemid) REFERENCES storeitems(storeitemid);
 X   ALTER TABLE ONLY public.storepurchases DROP CONSTRAINT storepurchases_storeitemid_fkey;
       public       postgres    false    1579    1909    1577            �           2606    26752    winlossrecords_playerid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY winlossrecords
    ADD CONSTRAINT winlossrecords_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);
 U   ALTER TABLE ONLY public.winlossrecords DROP CONSTRAINT winlossrecords_playerid_fkey;
       public       postgres    false    1584    1901    1570            �           2606    26757    winlossrecords_serviceid_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY winlossrecords
    ADD CONSTRAINT winlossrecords_serviceid_fkey FOREIGN KEY (serviceid) REFERENCES services(serviceid);
 V   ALTER TABLE ONLY public.winlossrecords DROP CONSTRAINT winlossrecords_serviceid_fkey;
       public       postgres    false    1582    1584    1913            �      x������ � �      �      x������ � �      �   Q   x�3404 N�̢�ה̒��<�4NC���\��Ģ�ԼԔ��J�䌢��
��Ģ�b�4��T�j=�^�����=... %}!      �      x������ � �      �      x�3�4�4404 .#N#8;F��� BK      �   2   x�%ɱ  �995��3�Q�,Y(
����4&R,�_���F�$��u      �   4   x�3�I-.QpIM��4�4�2E�qz����+���pE��T��qqq �a      �   �  x����r�0Ek�+P&���Ń ݸ��8)2��H�"�pHŎ��e%�<��ݹ8��`��:t���N�����={�gH[0[J9a�D.Mf� ���p�[�-jN�C�K��&VI���@fi��BV���k��t�ݩ��}3��4��\'x����Z���1���g~��6��/�/���k�4����1^�$��8>C��Stw{�mӭf[�a��r
��HQ;^�������;��fX� 2 J�Z�3'�UN��N�2:(�}l�6B9�e#R��!�s�Q=�2�F��u�D�V`4��pl�0����"Ŕ)VX���Zz�r��L�����f����{��R���@����^�N��鿨u8^�UYb��^����l�|�Vƙ3-Z�T�R�O{�;�>$����[�)      �   B   x�3�L5,K��"C.΀̊�T����J���!�)�wjnQbf�o�A9�SS`&��qqq �I�      �      x������ � �      �   -   x�3�L��JL��2�t�(��S��2��LVI��T�=... ��
(      �      x������ � �      �      x������ � �      �      x�3�2����� l       �      x������ � �     