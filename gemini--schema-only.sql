--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

--
-- Name: ccg_clone_deck(bigint, bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ccg_clone_deck(pplayerid bigint, pdeckid bigint) RETURNS bigint
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


ALTER FUNCTION public.ccg_clone_deck(pplayerid bigint, pdeckid bigint) OWNER TO postgres;

--
-- Name: ccg_create_deck(bigint, integer, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ccg_create_deck(pplayerid bigint, pserviceid integer, pdeckname character varying) RETURNS bigint
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


ALTER FUNCTION public.ccg_create_deck(pplayerid bigint, pserviceid integer, pdeckname character varying) OWNER TO postgres;

--
-- Name: ccg_delete_deck(bigint, integer, bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ccg_delete_deck(pplayerid bigint, pserviceid integer, pdeckid bigint) RETURNS SETOF void
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


ALTER FUNCTION public.ccg_delete_deck(pplayerid bigint, pserviceid integer, pdeckid bigint) OWNER TO postgres;

--
-- Name: ccg_get_sets(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ccg_get_sets(pplayerid bigint) RETURNS SETOF integer
    LANGUAGE plpgsql ROWS 10
    AS $$
		
  begin
        create temporary table temp_ccg_get_sets (setid integer not null) on commit drop;
	insert into temp_ccg_get_sets (setid) (select setid from ccgsetsforplayers where playerid = pplayerid);
	insert into temp_ccg_get_sets (setid) (select setid from ccgsetsforsubs where subscriptionid = (select subscriptionid from playeraccounts where playerid = pplayerid));
	return query select distinct setid from temp_ccg_get_sets;
  end;
 $$;


ALTER FUNCTION public.ccg_get_sets(pplayerid bigint) OWNER TO postgres;

--
-- Name: ccg_get_usable_cards(bigint, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION ccg_get_usable_cards(pplayerid bigint, pserviceid integer) RETURNS SETOF bigint
    LANGUAGE plpgsql
    AS $$

  begin
	return query select distinct definitionid from card_in_set where 
	(setid in (select setid from card_setsforplayers where playerid = pplayerid) or setid in (select setid from card_setsforsubs where subscriptionid = 
	(select subscriptionid from playeraccounts where playerid = pplayerid))) and setid in (select setid from card_sets where serviceid = pserviceid);
end;
 $$;


ALTER FUNCTION public.ccg_get_usable_cards(pplayerid bigint, pserviceid integer) OWNER TO postgres;

--
-- Name: create_account(character varying, character varying, character varying, character varying, boolean, character varying, character varying, integer, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_account(pusername character varying, ppassword character varying, ppassword2 character varying, pemail character varying, pcoppa boolean, pip character varying, pclient character varying, pgender integer, planguage character varying) RETURNS SETOF character varying
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


ALTER FUNCTION public.create_account(pusername character varying, ppassword character varying, ppassword2 character varying, pemail character varying, pcoppa boolean, pip character varying, pclient character varying, pgender integer, planguage character varying) OWNER TO postgres;

--
-- Name: encrypt(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION encrypt(ppassword character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
  begin
	--TODO make this not suck
	return ppassword;
  end;
 $$;


ALTER FUNCTION public.encrypt(ppassword character varying) OWNER TO postgres;

--
-- Name: forgotpass_maketoken(character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION forgotpass_maketoken(pemail character varying, pip character varying) RETURNS SETOF character varying
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


ALTER FUNCTION public.forgotpass_maketoken(pemail character varying, pip character varying) OWNER TO postgres;

--
-- Name: forgotpass_redeem(character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION forgotpass_redeem(pemail character varying, ppassword character varying, ppassword2 character varying, ptoken character varying, pip character varying, pclient character varying) RETURNS SETOF character varying
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


ALTER FUNCTION public.forgotpass_redeem(pemail character varying, ppassword character varying, ppassword2 character varying, ptoken character varying, pip character varying, pclient character varying) OWNER TO postgres;

--
-- Name: log_in(character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION log_in(pusername character varying, ppassword character varying, pip character varying, pclient character varying) RETURNS character varying
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


ALTER FUNCTION public.log_in(pusername character varying, ppassword character varying, pip character varying, pclient character varying) OWNER TO postgres;

--
-- Name: maketoken(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION maketoken(player_id bigint) RETURNS SETOF character varying
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


ALTER FUNCTION public.maketoken(player_id bigint) OWNER TO postgres;

--
-- Name: password_strength_check(character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION password_strength_check(ppassword character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$

  declare
	
	
  begin
	if (char_length(ppassword) < 6) then
		raise exception 'Password is too short.';
	end if;
		
  end;
 $$;


ALTER FUNCTION public.password_strength_check(ppassword character varying) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: winlossrecords; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE winlossrecords (
    winlossrecordid integer NOT NULL,
    playerid bigint NOT NULL,
    serviceid integer NOT NULL,
    wins integer DEFAULT 0 NOT NULL,
    losses integer DEFAULT 0 NOT NULL,
    draws integer DEFAULT 0 NOT NULL,
    rating integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.winlossrecords OWNER TO postgres;

--
-- Name: set_winlossrecord(bigint, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION set_winlossrecord(pplayerid bigint, pserviceid integer, pwins integer, plosses integer, pdraws integer) RETURNS SETOF winlossrecords
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


ALTER FUNCTION public.set_winlossrecord(pplayerid bigint, pserviceid integer, pwins integer, plosses integer, pdraws integer) OWNER TO postgres;

--
-- Name: verifyemail_maketoken(character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION verifyemail_maketoken(pemail character varying, pip character varying) RETURNS SETOF character varying
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


ALTER FUNCTION public.verifyemail_maketoken(pemail character varying, pip character varying) OWNER TO postgres;

--
-- Name: verifyemail_redeem(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION verifyemail_redeem(pemail character varying, ptoken character varying, pip character varying) RETURNS void
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


ALTER FUNCTION public.verifyemail_redeem(pemail character varying, ptoken character varying, pip character varying) OWNER TO postgres;

--
-- Name: bannedip; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE bannedip (
    bannedip character varying NOT NULL,
    banreason character varying NOT NULL,
    banneduntil timestamp without time zone,
    bannedipid integer NOT NULL
);


ALTER TABLE public.bannedip OWNER TO postgres;

--
-- Name: bannedip_bannedipid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bannedip_bannedipid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bannedip_bannedipid_seq OWNER TO postgres;

--
-- Name: bannedip_bannedipid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bannedip_bannedipid_seq OWNED BY bannedip.bannedipid;


--
-- Name: card_in_set; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE card_in_set (
    definitionid integer NOT NULL,
    setid integer NOT NULL,
    cardinsetid bigint NOT NULL
);


ALTER TABLE public.card_in_set OWNER TO postgres;

--
-- Name: card_in_set_cardinsetid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE card_in_set_cardinsetid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.card_in_set_cardinsetid_seq OWNER TO postgres;

--
-- Name: card_in_set_cardinsetid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE card_in_set_cardinsetid_seq OWNED BY card_in_set.cardinsetid;


--
-- Name: card_sets; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE card_sets (
    setid integer NOT NULL,
    englishname character varying NOT NULL,
    promo boolean DEFAULT false NOT NULL,
    serviceid integer,
    javaclass character varying NOT NULL
);


ALTER TABLE public.card_sets OWNER TO postgres;

--
-- Name: card_sets_setid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE card_sets_setid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.card_sets_setid_seq OWNER TO postgres;

--
-- Name: card_sets_setid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE card_sets_setid_seq OWNED BY card_sets.setid;


--
-- Name: card_setsforplayers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE card_setsforplayers (
    setforplayerid bigint NOT NULL,
    playerid bigint,
    availableuntil timestamp without time zone,
    setid integer NOT NULL
);


ALTER TABLE public.card_setsforplayers OWNER TO postgres;

--
-- Name: card_setsforsubs; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE card_setsforsubs (
    setforsubid integer NOT NULL,
    subscriptionid integer NOT NULL,
    setid integer NOT NULL
);


ALTER TABLE public.card_setsforsubs OWNER TO postgres;

--
-- Name: cards_in_set_cardid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE cards_in_set_cardid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cards_in_set_cardid_seq OWNER TO postgres;

--
-- Name: cards_in_set_cardid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE cards_in_set_cardid_seq OWNED BY card_in_set.definitionid;


--
-- Name: ccg_deckcards; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ccg_deckcards (
    cardindeckid bigint NOT NULL,
    deckid bigint NOT NULL,
    definitionid bigint NOT NULL,
    qty integer NOT NULL
);


ALTER TABLE public.ccg_deckcards OWNER TO postgres;

--
-- Name: ccg_decks; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE ccg_decks (
    deckid bigint NOT NULL,
    deckname character varying NOT NULL,
    playerid bigint,
    serviceid integer NOT NULL
);


ALTER TABLE public.ccg_decks OWNER TO postgres;

--
-- Name: ccgcardsindecks_cardindeckid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ccgcardsindecks_cardindeckid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ccgcardsindecks_cardindeckid_seq OWNER TO postgres;

--
-- Name: ccgcardsindecks_cardindeckid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ccgcardsindecks_cardindeckid_seq OWNED BY ccg_deckcards.cardindeckid;


--
-- Name: ccgdecks_deckid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ccgdecks_deckid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ccgdecks_deckid_seq OWNER TO postgres;

--
-- Name: ccgdecks_deckid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ccgdecks_deckid_seq OWNED BY ccg_decks.deckid;


--
-- Name: ccgsetsforplayers_setforplayerid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ccgsetsforplayers_setforplayerid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ccgsetsforplayers_setforplayerid_seq OWNER TO postgres;

--
-- Name: ccgsetsforplayers_setforplayerid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ccgsetsforplayers_setforplayerid_seq OWNED BY card_setsforplayers.setforplayerid;


--
-- Name: ccgsetsforsubs_setforsubid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ccgsetsforsubs_setforsubid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ccgsetsforsubs_setforsubid_seq OWNER TO postgres;

--
-- Name: ccgsetsforsubs_setforsubid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ccgsetsforsubs_setforsubid_seq OWNED BY card_setsforsubs.setforsubid;


--
-- Name: email_template; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE email_template (
    emailtemplateid integer NOT NULL,
    templatename character varying NOT NULL,
    language character varying NOT NULL,
    plaintext character varying NOT NULL,
    html character varying NOT NULL,
    subject character varying NOT NULL
);


ALTER TABLE public.email_template OWNER TO postgres;

--
-- Name: email_template_emailtemplateid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE email_template_emailtemplateid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.email_template_emailtemplateid_seq OWNER TO postgres;

--
-- Name: email_template_emailtemplateid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE email_template_emailtemplateid_seq OWNED BY email_template.emailtemplateid;


--
-- Name: guildinvites; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE guildinvites (
    guildinviteid bigint NOT NULL,
    guildid bigint NOT NULL,
    playerinvited bigint NOT NULL,
    playerinvitedby bigint NOT NULL,
    dateinvited timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.guildinvites OWNER TO postgres;

--
-- Name: guildinvites_guildinviteid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE guildinvites_guildinviteid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.guildinvites_guildinviteid_seq OWNER TO postgres;

--
-- Name: guildinvites_guildinviteid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE guildinvites_guildinviteid_seq OWNED BY guildinvites.guildinviteid;


--
-- Name: guildranks; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE guildranks (
    guildrankid bigint NOT NULL,
    guildid bigint NOT NULL,
    ranktitle character varying NOT NULL,
    caninvite boolean DEFAULT false NOT NULL,
    cankicklowerrank boolean DEFAULT false NOT NULL,
    canedit boolean DEFAULT false NOT NULL,
    ranklevel integer NOT NULL
);


ALTER TABLE public.guildranks OWNER TO postgres;

--
-- Name: guildranks_guildrankid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE guildranks_guildrankid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.guildranks_guildrankid_seq OWNER TO postgres;

--
-- Name: guildranks_guildrankid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE guildranks_guildrankid_seq OWNED BY guildranks.guildrankid;


--
-- Name: guilds; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE guilds (
    guildid bigint NOT NULL,
    name character varying NOT NULL,
    charter character varying,
    website character varying,
    founder bigint NOT NULL,
    datefounded timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.guilds OWNER TO postgres;

--
-- Name: guilds_guildid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE guilds_guildid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.guilds_guildid_seq OWNER TO postgres;

--
-- Name: guilds_guildid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE guilds_guildid_seq OWNED BY guilds.guildid;


--
-- Name: playeraccounts; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE playeraccounts (
    accountid bigint NOT NULL,
    playerid bigint NOT NULL,
    email character varying NOT NULL,
    subscribersince timestamp without time zone,
    createdon timestamp without time zone DEFAULT now() NOT NULL,
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
    verifiedemail timestamp without time zone,
    stripecustomerid character varying,
    admin boolean DEFAULT false NOT NULL
);


ALTER TABLE public.playeraccounts OWNER TO postgres;

--
-- Name: playeraccounts_accountid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE playeraccounts_accountid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.playeraccounts_accountid_seq OWNER TO postgres;

--
-- Name: playeraccounts_accountid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE playeraccounts_accountid_seq OWNED BY playeraccounts.accountid;


--
-- Name: playerbadges; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE playerbadges (
    playerbadgeid bigint NOT NULL,
    badgeid integer NOT NULL,
    playerid bigint NOT NULL,
    dateawarded timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.playerbadges OWNER TO postgres;

--
-- Name: playerbadges_playerbadgeid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE playerbadges_playerbadgeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.playerbadges_playerbadgeid_seq OWNER TO postgres;

--
-- Name: playerbadges_playerbadgeid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE playerbadges_playerbadgeid_seq OWNED BY playerbadges.playerbadgeid;


--
-- Name: players; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE players (
    playerid bigint NOT NULL,
    username character varying NOT NULL,
    language character varying DEFAULT 'en'::character varying NOT NULL,
    gender integer DEFAULT 1 NOT NULL,
    guildid bigint,
    guildrankid bigint
);


ALTER TABLE public.players OWNER TO postgres;

--
-- Name: players_playerid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE players_playerid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.players_playerid_seq OWNER TO postgres;

--
-- Name: players_playerid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE players_playerid_seq OWNED BY players.playerid;


--
-- Name: promocodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE promocodes (
    promoid integer NOT NULL,
    promocode character varying NOT NULL,
    expirationdate timestamp without time zone,
    usesallowed integer,
    storeitemid integer NOT NULL,
    discountpercent double precision DEFAULT 1 NOT NULL
);


ALTER TABLE public.promocodes OWNER TO postgres;

--
-- Name: services; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE services (
    serviceid integer NOT NULL,
    servicename character varying NOT NULL
);


ALTER TABLE public.services OWNER TO postgres;

--
-- Name: services_serviceid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE services_serviceid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.services_serviceid_seq OWNER TO postgres;

--
-- Name: services_serviceid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE services_serviceid_seq OWNED BY services.serviceid;


--
-- Name: storecatalog; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE storecatalog (
    storecatalogid integer NOT NULL,
    storeitemid integer NOT NULL,
    language character varying NOT NULL,
    title character varying NOT NULL,
    description character varying NOT NULL
);


ALTER TABLE public.storecatalog OWNER TO postgres;

--
-- Name: storecatalog_storecatalogid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE storecatalog_storecatalogid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.storecatalog_storecatalogid_seq OWNER TO postgres;

--
-- Name: storecatalog_storecatalogid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE storecatalog_storecatalogid_seq OWNED BY storecatalog.storecatalogid;


--
-- Name: storeitems; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE storeitems (
    storeitemid integer NOT NULL,
    availablestart timestamp without time zone DEFAULT now() NOT NULL,
    availableend timestamp without time zone,
    stocklimit integer,
    detailsurl character varying,
    show_in_store boolean DEFAULT true NOT NULL,
    setid integer,
    subscriptionid integer,
    pricecents integer NOT NULL
);


ALTER TABLE public.storeitems OWNER TO postgres;

--
-- Name: storepurchases; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE storepurchases (
    purchaseid bigint NOT NULL,
    storeitemid integer NOT NULL,
    playerid bigint NOT NULL,
    purchasedfromip character varying,
    promoid integer,
    purchasedate timestamp with time zone DEFAULT now() NOT NULL,
    stripeinvoice character varying,
    pricecents integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.storepurchases OWNER TO postgres;

--
-- Name: storepurchases_purchaseid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE storepurchases_purchaseid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.storepurchases_purchaseid_seq OWNER TO postgres;

--
-- Name: storepurchases_purchaseid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE storepurchases_purchaseid_seq OWNED BY storepurchases.purchaseid;


--
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subscriptions (
    subscriptionid integer NOT NULL,
    monthlyplankey character varying,
    months integer
);


ALTER TABLE public.subscriptions OWNER TO postgres;

--
-- Name: subscriptions_subscriptionid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE subscriptions_subscriptionid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.subscriptions_subscriptionid_seq OWNER TO postgres;

--
-- Name: subscriptions_subscriptionid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE subscriptions_subscriptionid_seq OWNED BY subscriptions.subscriptionid;


--
-- Name: winlossrecords_winlossrecordid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE winlossrecords_winlossrecordid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.winlossrecords_winlossrecordid_seq OWNER TO postgres;

--
-- Name: winlossrecords_winlossrecordid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE winlossrecords_winlossrecordid_seq OWNED BY winlossrecords.winlossrecordid;


--
-- Name: bannedipid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE bannedip ALTER COLUMN bannedipid SET DEFAULT nextval('bannedip_bannedipid_seq'::regclass);


--
-- Name: cardinsetid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE card_in_set ALTER COLUMN cardinsetid SET DEFAULT nextval('card_in_set_cardinsetid_seq'::regclass);


--
-- Name: setid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE card_sets ALTER COLUMN setid SET DEFAULT nextval('card_sets_setid_seq'::regclass);


--
-- Name: setforplayerid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE card_setsforplayers ALTER COLUMN setforplayerid SET DEFAULT nextval('ccgsetsforplayers_setforplayerid_seq'::regclass);


--
-- Name: setforsubid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE card_setsforsubs ALTER COLUMN setforsubid SET DEFAULT nextval('ccgsetsforsubs_setforsubid_seq'::regclass);


--
-- Name: cardindeckid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ccg_deckcards ALTER COLUMN cardindeckid SET DEFAULT nextval('ccgcardsindecks_cardindeckid_seq'::regclass);


--
-- Name: deckid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ccg_decks ALTER COLUMN deckid SET DEFAULT nextval('ccgdecks_deckid_seq'::regclass);


--
-- Name: emailtemplateid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE email_template ALTER COLUMN emailtemplateid SET DEFAULT nextval('email_template_emailtemplateid_seq'::regclass);


--
-- Name: guildinviteid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE guildinvites ALTER COLUMN guildinviteid SET DEFAULT nextval('guildinvites_guildinviteid_seq'::regclass);


--
-- Name: guildrankid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE guildranks ALTER COLUMN guildrankid SET DEFAULT nextval('guildranks_guildrankid_seq'::regclass);


--
-- Name: guildid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE guilds ALTER COLUMN guildid SET DEFAULT nextval('guilds_guildid_seq'::regclass);


--
-- Name: accountid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE playeraccounts ALTER COLUMN accountid SET DEFAULT nextval('playeraccounts_accountid_seq'::regclass);


--
-- Name: playerbadgeid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE playerbadges ALTER COLUMN playerbadgeid SET DEFAULT nextval('playerbadges_playerbadgeid_seq'::regclass);


--
-- Name: playerid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE players ALTER COLUMN playerid SET DEFAULT nextval('players_playerid_seq'::regclass);


--
-- Name: serviceid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE services ALTER COLUMN serviceid SET DEFAULT nextval('services_serviceid_seq'::regclass);


--
-- Name: storecatalogid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE storecatalog ALTER COLUMN storecatalogid SET DEFAULT nextval('storecatalog_storecatalogid_seq'::regclass);


--
-- Name: purchaseid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE storepurchases ALTER COLUMN purchaseid SET DEFAULT nextval('storepurchases_purchaseid_seq'::regclass);


--
-- Name: subscriptionid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE subscriptions ALTER COLUMN subscriptionid SET DEFAULT nextval('subscriptions_subscriptionid_seq'::regclass);


--
-- Name: winlossrecordid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE winlossrecords ALTER COLUMN winlossrecordid SET DEFAULT nextval('winlossrecords_winlossrecordid_seq'::regclass);


--
-- Name: card_in_set_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY card_in_set
    ADD CONSTRAINT card_in_set_pkey PRIMARY KEY (cardinsetid);


--
-- Name: card_sets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY card_sets
    ADD CONSTRAINT card_sets_pkey PRIMARY KEY (setid);


--
-- Name: ccgcardsindecks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ccg_deckcards
    ADD CONSTRAINT ccgcardsindecks_pkey PRIMARY KEY (cardindeckid);


--
-- Name: ccgdecks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY ccg_decks
    ADD CONSTRAINT ccgdecks_pkey PRIMARY KEY (deckid);


--
-- Name: ccgsetsforplayers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY card_setsforplayers
    ADD CONSTRAINT ccgsetsforplayers_pkey PRIMARY KEY (setforplayerid);


--
-- Name: ccgsetsforsubs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY card_setsforsubs
    ADD CONSTRAINT ccgsetsforsubs_pkey PRIMARY KEY (setforsubid);


--
-- Name: email_template_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY email_template
    ADD CONSTRAINT email_template_pkey PRIMARY KEY (emailtemplateid);


--
-- Name: guildinvites_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY guildinvites
    ADD CONSTRAINT guildinvites_pkey PRIMARY KEY (guildinviteid);


--
-- Name: guildranks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY guildranks
    ADD CONSTRAINT guildranks_pkey PRIMARY KEY (guildrankid);


--
-- Name: guilds_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY guilds
    ADD CONSTRAINT guilds_pkey PRIMARY KEY (guildid);


--
-- Name: playeraccounts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_pkey PRIMARY KEY (accountid);


--
-- Name: playerbadges_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY playerbadges
    ADD CONSTRAINT playerbadges_pkey PRIMARY KEY (playerbadgeid);


--
-- Name: players_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY players
    ADD CONSTRAINT players_pkey PRIMARY KEY (playerid);


--
-- Name: promocodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY promocodes
    ADD CONSTRAINT promocodes_pkey PRIMARY KEY (promoid);


--
-- Name: services_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY services
    ADD CONSTRAINT services_pkey PRIMARY KEY (serviceid);


--
-- Name: storecatalog_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY storecatalog
    ADD CONSTRAINT storecatalog_pkey PRIMARY KEY (storecatalogid);


--
-- Name: storeitems_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY storeitems
    ADD CONSTRAINT storeitems_pkey PRIMARY KEY (storeitemid);


--
-- Name: storepurchases_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_pkey PRIMARY KEY (purchaseid);


--
-- Name: subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (subscriptionid);


--
-- Name: winlossrecords_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY winlossrecords
    ADD CONSTRAINT winlossrecords_pkey PRIMARY KEY (winlossrecordid);


--
-- Name: card_sets_serviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY card_sets
    ADD CONSTRAINT card_sets_serviceid_fkey FOREIGN KEY (serviceid) REFERENCES services(serviceid);


--
-- Name: card_setsforplayers_setid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY card_setsforplayers
    ADD CONSTRAINT card_setsforplayers_setid_fkey FOREIGN KEY (setid) REFERENCES card_sets(setid);


--
-- Name: card_setsforsubs_setid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY card_setsforsubs
    ADD CONSTRAINT card_setsforsubs_setid_fkey FOREIGN KEY (setid) REFERENCES card_sets(setid);


--
-- Name: cards_in_set_setid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY card_in_set
    ADD CONSTRAINT cards_in_set_setid_fkey FOREIGN KEY (setid) REFERENCES card_sets(setid);


--
-- Name: ccg_decks_serviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ccg_decks
    ADD CONSTRAINT ccg_decks_serviceid_fkey FOREIGN KEY (serviceid) REFERENCES services(serviceid);


--
-- Name: ccgcardsindecks_deckid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ccg_deckcards
    ADD CONSTRAINT ccgcardsindecks_deckid_fkey FOREIGN KEY (deckid) REFERENCES ccg_decks(deckid);


--
-- Name: ccgdecks_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ccg_decks
    ADD CONSTRAINT ccgdecks_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: ccgsetsforplayers_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY card_setsforplayers
    ADD CONSTRAINT ccgsetsforplayers_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: ccgsetsforsubs_subscriptionid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY card_setsforsubs
    ADD CONSTRAINT ccgsetsforsubs_subscriptionid_fkey FOREIGN KEY (subscriptionid) REFERENCES subscriptions(subscriptionid);


--
-- Name: guildinvites_guildid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY guildinvites
    ADD CONSTRAINT guildinvites_guildid_fkey FOREIGN KEY (guildid) REFERENCES guilds(guildid);


--
-- Name: guildinvites_playerinvited_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY guildinvites
    ADD CONSTRAINT guildinvites_playerinvited_fkey FOREIGN KEY (playerinvited) REFERENCES players(playerid);


--
-- Name: guildinvites_playerinvitedby_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY guildinvites
    ADD CONSTRAINT guildinvites_playerinvitedby_fkey FOREIGN KEY (playerinvitedby) REFERENCES players(playerid);


--
-- Name: guildranks_guildid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY guildranks
    ADD CONSTRAINT guildranks_guildid_fkey FOREIGN KEY (guildid) REFERENCES guilds(guildid);


--
-- Name: guilds_founder_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY guilds
    ADD CONSTRAINT guilds_founder_fkey FOREIGN KEY (founder) REFERENCES players(playerid);


--
-- Name: playeraccounts_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: playeraccounts_subscriptionid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_subscriptionid_fkey FOREIGN KEY (subscriptionid) REFERENCES subscriptions(subscriptionid);


--
-- Name: playerbadges_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY playerbadges
    ADD CONSTRAINT playerbadges_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: players_guildid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY players
    ADD CONSTRAINT players_guildid_fkey FOREIGN KEY (guildid) REFERENCES guilds(guildid);


--
-- Name: players_guildrankid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY players
    ADD CONSTRAINT players_guildrankid_fkey FOREIGN KEY (guildrankid) REFERENCES guildranks(guildrankid);


--
-- Name: promocodes_storeitemid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY promocodes
    ADD CONSTRAINT promocodes_storeitemid_fkey FOREIGN KEY (storeitemid) REFERENCES storeitems(storeitemid);


--
-- Name: storecatalog_storeitemid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storecatalog
    ADD CONSTRAINT storecatalog_storeitemid_fkey FOREIGN KEY (storeitemid) REFERENCES storeitems(storeitemid);


--
-- Name: storeitems_setid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storeitems
    ADD CONSTRAINT storeitems_setid_fkey FOREIGN KEY (setid) REFERENCES card_sets(setid);


--
-- Name: storeitems_subscriptionid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storeitems
    ADD CONSTRAINT storeitems_subscriptionid_fkey FOREIGN KEY (subscriptionid) REFERENCES subscriptions(subscriptionid);


--
-- Name: storepurchases_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: storepurchases_promoid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_promoid_fkey FOREIGN KEY (promoid) REFERENCES promocodes(promoid);


--
-- Name: storepurchases_storeitemid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_storeitemid_fkey FOREIGN KEY (storeitemid) REFERENCES storeitems(storeitemid);


--
-- Name: winlossrecords_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY winlossrecords
    ADD CONSTRAINT winlossrecords_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- Name: winlossrecords_serviceid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY winlossrecords
    ADD CONSTRAINT winlossrecords_serviceid_fkey FOREIGN KEY (serviceid) REFERENCES services(serviceid);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

