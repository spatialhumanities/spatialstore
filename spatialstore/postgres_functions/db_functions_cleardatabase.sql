--------------------
-- Clear Database --
--------------------

-- Function: cleardatabase()

-- DROP FUNCTION cleardatabase();

CREATE OR REPLACE FUNCTION cleardatabase()
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	-- Datenbanktabellen löschen --
	DROP TABLE transformation CASCADE;
	DROP TABLE spatialcontext CASCADE;
	DROP TABLE media CASCADE;
	DROP TABLE viewpoint CASCADE;
	DROP TABLE pointcloud CASCADE;
	DROP TABLE panorama CASCADE;
	DROP TABLE panoramaimg CASCADE;
	DROP TABLE feature CASCADE;
	DROP TABLE edit CASCADE;
	DROP TABLE import CASCADE;
	DROP TABLE imgmeasurement CASCADE;
	DROP TABLE imgmeasurement_point CASCADE;
	DROP TABLE feature_viewpoint CASCADE;

	-- Neue Tabellen anlegen --
	CREATE TABLE transformation (
		id SERIAL PRIMARY KEY,
		srcCRS character varying, 
		dstCRS character varying, 
		param character varying
	);

	CREATE TABLE spatialcontext (
		id SERIAL PRIMARY KEY,
		srid integer,
		IDREF_trans integer REFERENCES transformation (id) ON DELETE CASCADE,
		name character varying, 
		place character varying, 
		date date 
	);

	CREATE TABLE media (
		id SERIAL PRIMARY KEY,
		IDREF_sc integer REFERENCES spatialcontext (id) ON DELETE CASCADE,
		IDREF_trans integer REFERENCES transformation (id) ON DELETE CASCADE,
		filename character varying,
		type character varying,
		description character varying
	);

	CREATE TABLE viewpoint (
		id SERIAL PRIMARY KEY,
		IDREF_sc integer REFERENCES spatialcontext (id) ON DELETE CASCADE,
		IDREF_trans integer REFERENCES transformation (id) ON DELETE CASCADE,
		name character varying,
		place character varying
	);

	CREATE TABLE pointcloud (
		id SERIAL PRIMARY KEY,
		IDREF_trans integer REFERENCES transformation (id) ON DELETE CASCADE,
		IDREF_view integer REFERENCES viewpoint (id) ON DELETE CASCADE,
		filename character varying,
		type character varying,
		bbox_local character varying,
		bbox_global character varying, 
		bbox_polar character varying, 
		remissionrange character varying, 
		rows integer,
		cols integer
	);

	CREATE TABLE panorama (
		id SERIAL PRIMARY KEY,
		IDREF_view integer REFERENCES viewpoint (id) ON DELETE CASCADE, 
		IDREF_trans integer REFERENCES transformation (id) ON DELETE CASCADE,
		type character varying, 
		kindof character varying 
	);

	CREATE TABLE panoramaimg (
		id SERIAL PRIMARY KEY,
		IDREF_pano integer REFERENCES panorama (id) ON DELETE CASCADE, 
		img character varying, 
		imgorder integer 
	);

	CREATE TABLE feature (
		id SERIAL PRIMARY KEY,
		IDREF_sc integer REFERENCES spatialcontext (id) ON DELETE CASCADE,
		geom geometry
	);
	
		CREATE TABLE edit (
		id SERIAL PRIMARY KEY,
		IDREF_feature integer REFERENCES feature (id) ON DELETE CASCADE,
		creator character varying,
		date timestamp
	);

	CREATE TABLE import (
		id SERIAL PRIMARY KEY ,		
		IDREF_edit integer REFERENCES edit (id) ON DELETE CASCADE,
		license character varying
	);

	CREATE TABLE imgmeasurement (
		id SERIAL PRIMARY KEY,
		IDREF_pano integer REFERENCES panorama (id) ON DELETE CASCADE,
		IDREF_edit integer REFERENCES edit (id) ON DELETE CASCADE,		 
		zoom double precision, 
		resolution double precision,
		geomtype character varying		
	);

	CREATE TABLE imgmeasurement_point (
		id SERIAL PRIMARY KEY,
		IDREF_imgmeas integer REFERENCES imgmeasurement (id) ON DELETE CASCADE,
		theta double precision,
		phi double precision,
		pointorder integer
	);

	CREATE TABLE feature_viewpoint (
		IDREF_feature integer REFERENCES feature (id) ON DELETE CASCADE,
		IDREF_view integer REFERENCES viewpoint (id) ON DELETE CASCADE
	);

	RETURN true;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cleardatabase()
  OWNER TO postgres;

-- Function: cleardatabase(text)

-- DROP FUNCTION cleardatabase(text);

CREATE OR REPLACE FUNCTION cleardatabase(spatialcon text)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	-- Datenbanktabellen löschen --
	DELETE FROM spatialcontext s WHERE s.name = spatialcon;

	RETURN true;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cleardatabase(text)
  OWNER TO postgres;

-- Function: getsetoftransformationids()

-- DROP FUNCTION getsetoftransformationids();

CREATE OR REPLACE FUNCTION getsetoftransformationids()
  RETURNS SETOF integer AS
$BODY$select id from transformation
$BODY$
  LANGUAGE sql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION getsetoftransformationids()
  OWNER TO postgres;

-- Function: cleartrans(numeric)

-- DROP FUNCTION cleartrans(numeric);

CREATE OR REPLACE FUNCTION cleartrans(transid numeric)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

DECLARE sctrans boolean;
DECLARE medtrans boolean;
DECLARE vptrans boolean;
DECLARE pctrans boolean;
DECLARE pantrans boolean;

BEGIN 

	SELECT * INTO sctrans FROM
	( 
		SELECT EXISTS(SELECT t.id FROM transformation t, spatialcontext s WHERE s.IDREF_trans = transid)
		
	) AS result1;

	SELECT * INTO medtrans FROM
	( 
		SELECT EXISTS(SELECT t.id FROM transformation t, media m WHERE m.IDREF_trans = transid)
		
	) AS result2;

	SELECT * INTO vptrans FROM
	( 
		SELECT EXISTS(SELECT t.id FROM transformation t, viewpoint v WHERE v.IDREF_trans = transid)
		
	) AS result3;

	SELECT * INTO pctrans FROM
	( 
		SELECT EXISTS(SELECT t.id FROM transformation t, pointcloud pc WHERE pc.IDREF_trans = transid)
		
	) AS result4;

	SELECT * INTO pantrans FROM
	( 
		SELECT EXISTS(SELECT t.id FROM transformation t, panorama p WHERE p.IDREF_trans = transid)
		
	) AS result5;

	IF NOT sctrans AND NOT medtrans AND NOT vptrans AND NOT pctrans AND NOT pantrans THEN
		DELETE FROM transformation t WHERE t.id = transid;
		ret = false;
	ELSE
		ret = true;
	END IF;

    RETURN ret;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cleartrans(numeric)
  OWNER TO postgres;