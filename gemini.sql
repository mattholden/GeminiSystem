--
-- PostgreSQL database dump
--

-- Dumped from database version 9.0.4
-- Dumped by pg_dump version 9.0.3
-- Started on 2012-09-13 01:39:15

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 1859 (class 1262 OID 26497)
-- Name: gemini; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE gemini WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'English_United States.1252' LC_CTYPE = 'English_United States.1252';


ALTER DATABASE gemini OWNER TO postgres;

\connect gemini

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 337 (class 2612 OID 11574)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

--
-- TOC entry 18 (class 1255 OID 26696)
-- Dependencies: 5 337
-- Name: forgotpass_maketoken(character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION forgotpass_maketoken(pusername character varying, pemail character varying) RETURNS SETOF character varying
    LANGUAGE plpgsql
    AS $$

  declare
	token character varying;	
	player_id bigint;
	
  begin
	select players.playerid into player_id from playeraccounts join players on (players.playerid = playeraccounts.playerid) where email = pemail and username = pusername;

	if (player_id is null) then
		raise exception 'UserOrEmailNotFound';
	end if;
	
	token = maketoken(player_id);
	update playeraccounts set forgotpasstoken = token, forgotpassexpires = now() + interval '3 hours' where playerid = player_id;
	
	-- successful login
	return next token;
  end;
 $$;


ALTER FUNCTION public.forgotpass_maketoken(pusername character varying, pemail character varying) OWNER TO postgres;

--
-- TOC entry 20 (class 1255 OID 26694)
-- Dependencies: 337 5
-- Name: log_in(character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION log_in(pusername character varying, ppassword character varying, pip character varying, pclient character varying) RETURNS SETOF character varying
    LANGUAGE plpgsql ROWS 1
    AS $$

  declare
	ban_until timestamp;
	ban_reason text;
	player_id bigint;
	badpass int;
	token character varying;
	token1 bigint;
	token2 bigint;
	token3 bigint;
	
  begin
	select into ban_until, ban_reason banneduntil, banreason from bannedip where bannedip.bannedip = pip;
	if (ban_until is not null and ban_until > now()) then
		raise exception 'BannedIP IP Address is banned.';
	end if;

	select into ban_until, ban_reason, player_id banneduntil, banreason, players.playerid from playeraccounts join players on (players.playerid = playeraccounts.playerid) where username = pusername and password = ppassword;
	if (player_id is null) then

		-- update the bad password attempts
		update playeraccounts set badpassattempts = badpassattempts + 1 where playerid = (select playerid from players where username = pusername);
		select badpassattempts into badpass from playeraccounts where playerid = (select playerid from players where username = pusername);
		if (badpass >= 3) then
			update playeraccounts set banneduntil = now() + interval '1 hour', banreason='Multiple consecutive login attempts with incorrect password' where playerid = (select playerid from players where username = pusername);
			insert into bannedip (bannedip, banneduntil, banreason) values (pip, now() + interval '1 hour', 'Multiple consecutive login attempts with incorrect password' );
		end if;
		token = null;
		return next token;
		
	end if;
	if (ban_until is not null and ban_until > now()) then
		raise 'BannedUser This user account is banned.';
	end if;

	-- successful login
	select maketoken(player_id) into token;
	update playeraccounts set banneduntil = null, banreason = null, lastlogintime = now(), lastloginip = pip, badpassattempts = 0, lastloginclient = pclient, forgotpasstoken = null, forgotpassexpires = null, sessiontoken = token where playerid = player_id;
	return next token;
  end;
 $$;


ALTER FUNCTION public.log_in(pusername character varying, ppassword character varying, pip character varying, pclient character varying) OWNER TO postgres;

--
-- TOC entry 19 (class 1255 OID 26695)
-- Dependencies: 337 5
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

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1536 (class 1259 OID 26610)
-- Dependencies: 5
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
-- TOC entry 1542 (class 1259 OID 26697)
-- Dependencies: 5 1536
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
-- TOC entry 1862 (class 0 OID 0)
-- Dependencies: 1542
-- Name: bannedip_bannedipid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bannedip_bannedipid_seq OWNED BY bannedip.bannedipid;


--
-- TOC entry 1538 (class 1259 OID 26620)
-- Dependencies: 1831 1832 1833 5
-- Name: playeraccounts; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE playeraccounts (
    accountid bigint NOT NULL,
    playerid bigint NOT NULL,
    email character varying NOT NULL,
    birthdate date NOT NULL,
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
    badpassattempts integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.playeraccounts OWNER TO postgres;

--
-- TOC entry 1537 (class 1259 OID 26618)
-- Dependencies: 5 1538
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
-- TOC entry 1863 (class 0 OID 0)
-- Dependencies: 1537
-- Name: playeraccounts_accountid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE playeraccounts_accountid_seq OWNED BY playeraccounts.accountid;


--
-- TOC entry 1530 (class 1259 OID 26500)
-- Dependencies: 1821 1822 1823 1824 1825 5
-- Name: players; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE players (
    playerid bigint NOT NULL,
    username character varying NOT NULL,
    language character varying DEFAULT 'en'::character varying NOT NULL,
    gender integer DEFAULT 1 NOT NULL,
    wins integer DEFAULT 0 NOT NULL,
    draws integer DEFAULT 0 NOT NULL,
    losses integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.players OWNER TO postgres;

--
-- TOC entry 1529 (class 1259 OID 26498)
-- Dependencies: 1530 5
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
-- TOC entry 1864 (class 0 OID 0)
-- Dependencies: 1529
-- Name: players_playerid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE players_playerid_seq OWNED BY players.playerid;


--
-- TOC entry 1533 (class 1259 OID 26580)
-- Dependencies: 5
-- Name: promocodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE promocodes (
    promoid integer NOT NULL,
    promocode character varying NOT NULL,
    expirationdate timestamp without time zone,
    usesallowed integer
);


ALTER TABLE public.promocodes OWNER TO postgres;

--
-- TOC entry 1535 (class 1259 OID 26590)
-- Dependencies: 1828 5
-- Name: promouses; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE promouses (
    promouseid integer NOT NULL,
    promoid integer NOT NULL,
    playerid bigint NOT NULL,
    usedatetime timestamp without time zone DEFAULT now() NOT NULL,
    useip character varying NOT NULL
);


ALTER TABLE public.promouses OWNER TO postgres;

--
-- TOC entry 1534 (class 1259 OID 26588)
-- Dependencies: 1535 5
-- Name: promouses_promouseid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE promouses_promouseid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.promouses_promouseid_seq OWNER TO postgres;

--
-- TOC entry 1865 (class 0 OID 0)
-- Dependencies: 1534
-- Name: promouses_promouseid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE promouses_promouseid_seq OWNED BY promouses.promouseid;


--
-- TOC entry 1539 (class 1259 OID 26641)
-- Dependencies: 1834 5
-- Name: storeitems; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE storeitems (
    storeitemid integer NOT NULL,
    priceusd money NOT NULL,
    availablestart timestamp without time zone DEFAULT now() NOT NULL,
    availableend timestamp without time zone,
    stocklimit integer,
    detailsurl character varying
);


ALTER TABLE public.storeitems OWNER TO postgres;

--
-- TOC entry 1541 (class 1259 OID 26652)
-- Dependencies: 1836 5
-- Name: storepurchases; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE storepurchases (
    purchaseid bigint NOT NULL,
    storeitemid integer NOT NULL,
    playerid bigint NOT NULL,
    transactionid bigint,
    purchasedon timestamp without time zone DEFAULT now() NOT NULL,
    purchasedfromip character varying
);


ALTER TABLE public.storepurchases OWNER TO postgres;

--
-- TOC entry 1540 (class 1259 OID 26650)
-- Dependencies: 1541 5
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
-- TOC entry 1866 (class 0 OID 0)
-- Dependencies: 1540
-- Name: storepurchases_purchaseid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE storepurchases_purchaseid_seq OWNED BY storepurchases.purchaseid;


--
-- TOC entry 1532 (class 1259 OID 26558)
-- Dependencies: 5
-- Name: subscriptions; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE subscriptions (
    subscriptionid integer NOT NULL
);


ALTER TABLE public.subscriptions OWNER TO postgres;

--
-- TOC entry 1531 (class 1259 OID 26556)
-- Dependencies: 5 1532
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
-- TOC entry 1867 (class 0 OID 0)
-- Dependencies: 1531
-- Name: subscriptions_subscriptionid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE subscriptions_subscriptionid_seq OWNED BY subscriptions.subscriptionid;


--
-- TOC entry 1829 (class 2604 OID 26699)
-- Dependencies: 1542 1536
-- Name: bannedipid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE bannedip ALTER COLUMN bannedipid SET DEFAULT nextval('bannedip_bannedipid_seq'::regclass);


--
-- TOC entry 1830 (class 2604 OID 26623)
-- Dependencies: 1537 1538 1538
-- Name: accountid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE playeraccounts ALTER COLUMN accountid SET DEFAULT nextval('playeraccounts_accountid_seq'::regclass);


--
-- TOC entry 1820 (class 2604 OID 26503)
-- Dependencies: 1529 1530 1530
-- Name: playerid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE players ALTER COLUMN playerid SET DEFAULT nextval('players_playerid_seq'::regclass);


--
-- TOC entry 1827 (class 2604 OID 26593)
-- Dependencies: 1534 1535 1535
-- Name: promouseid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE promouses ALTER COLUMN promouseid SET DEFAULT nextval('promouses_promouseid_seq'::regclass);


--
-- TOC entry 1835 (class 2604 OID 26655)
-- Dependencies: 1541 1540 1541
-- Name: purchaseid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE storepurchases ALTER COLUMN purchaseid SET DEFAULT nextval('storepurchases_purchaseid_seq'::regclass);


--
-- TOC entry 1826 (class 2604 OID 26561)
-- Dependencies: 1532 1531 1532
-- Name: subscriptionid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE subscriptions ALTER COLUMN subscriptionid SET DEFAULT nextval('subscriptions_subscriptionid_seq'::regclass);


--
-- TOC entry 1846 (class 2606 OID 26630)
-- Dependencies: 1538 1538
-- Name: playeraccounts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_pkey PRIMARY KEY (accountid);


--
-- TOC entry 1838 (class 2606 OID 26509)
-- Dependencies: 1530 1530
-- Name: players_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY players
    ADD CONSTRAINT players_pkey PRIMARY KEY (playerid);


--
-- TOC entry 1842 (class 2606 OID 26587)
-- Dependencies: 1533 1533
-- Name: promocodes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY promocodes
    ADD CONSTRAINT promocodes_pkey PRIMARY KEY (promoid);


--
-- TOC entry 1844 (class 2606 OID 26599)
-- Dependencies: 1535 1535
-- Name: promouses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY promouses
    ADD CONSTRAINT promouses_pkey PRIMARY KEY (promouseid);


--
-- TOC entry 1848 (class 2606 OID 26649)
-- Dependencies: 1539 1539
-- Name: storeitems_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY storeitems
    ADD CONSTRAINT storeitems_pkey PRIMARY KEY (storeitemid);


--
-- TOC entry 1850 (class 2606 OID 26661)
-- Dependencies: 1541 1541
-- Name: storepurchases_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_pkey PRIMARY KEY (purchaseid);


--
-- TOC entry 1840 (class 2606 OID 26563)
-- Dependencies: 1532 1532
-- Name: subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY subscriptions
    ADD CONSTRAINT subscriptions_pkey PRIMARY KEY (subscriptionid);


--
-- TOC entry 1853 (class 2606 OID 26631)
-- Dependencies: 1538 1837 1530
-- Name: playeraccounts_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- TOC entry 1854 (class 2606 OID 26636)
-- Dependencies: 1538 1839 1532
-- Name: playeraccounts_subscriptionid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY playeraccounts
    ADD CONSTRAINT playeraccounts_subscriptionid_fkey FOREIGN KEY (subscriptionid) REFERENCES subscriptions(subscriptionid);


--
-- TOC entry 1851 (class 2606 OID 26600)
-- Dependencies: 1535 1837 1530
-- Name: promouses_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY promouses
    ADD CONSTRAINT promouses_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- TOC entry 1852 (class 2606 OID 26605)
-- Dependencies: 1535 1841 1533
-- Name: promouses_promoid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY promouses
    ADD CONSTRAINT promouses_promoid_fkey FOREIGN KEY (promoid) REFERENCES promocodes(promoid);


--
-- TOC entry 1856 (class 2606 OID 26667)
-- Dependencies: 1541 1530 1837
-- Name: storepurchases_playerid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_playerid_fkey FOREIGN KEY (playerid) REFERENCES players(playerid);


--
-- TOC entry 1855 (class 2606 OID 26662)
-- Dependencies: 1539 1541 1847
-- Name: storepurchases_storeitemid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY storepurchases
    ADD CONSTRAINT storepurchases_storeitemid_fkey FOREIGN KEY (storeitemid) REFERENCES storeitems(storeitemid);


--
-- TOC entry 1861 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2012-09-13 01:39:16

--
-- PostgreSQL database dump complete
--

