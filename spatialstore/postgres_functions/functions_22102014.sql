--------------------
-- Input Database --
--------------------

-- Function: inputtransformation(text, text, text)

-- DROP FUNCTION inputtransformation(text, text, text);

CREATE OR REPLACE FUNCTION inputtransformation(src text, dst text, params text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO transformation (srccrs, dstcrs, param) VALUES 
		    (src, dst, params)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputtransformation(text, text, text)
  OWNER TO vagrant;
  
-- Function: inputspatialcontext(numeric, text, text, date)

-- DROP FUNCTION inputspatialcontext(numeric, text, text, date);

CREATE OR REPLACE FUNCTION inputspatialcontext(transid numeric, namesc text, placesc text, datesc date)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO spatialcontext (IDREF_trans, name, place, date) VALUES 
		    (transid, namesc, placesc, datesc)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputspatialcontext(numeric, text, text, date)
  OWNER TO vagrant;
  
CREATE OR REPLACE FUNCTION inputspatialcontext(transid numeric, namesc text, placesc text, datesc date, sridsc numeric)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO spatialcontext (IDREF_trans, name, place, date, srid) VALUES 
		    (transid, namesc, placesc, datesc, sridsc)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputspatialcontext(numeric, text, text, date, numeric)
  OWNER TO vagrant;


-- Function: inputmedia(numeric, numeric, text, text, text)

-- DROP FUNCTION inputmedia(numeric, numeric, text, text, text);

CREATE OR REPLACE FUNCTION inputmedia(transid numeric, scid numeric, mtype text, mfilename text, mdesc text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO media (IDREF_sc, IDREF_trans, filename, type, description) VALUES 
		    (scid, transid, mfilename, mtype, mdesc)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputmedia(numeric, numeric, text, text, text)
  OWNER TO vagrant;

-- Function: inputviewpoint(numeric, numeric, text, text)

-- DROP FUNCTION inputviewpoint(numeric, numeric, text, text);

CREATE OR REPLACE FUNCTION inputviewpoint(transid numeric, scid numeric, vname text, vplace text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO viewpoint (IDREF_sc, IDREF_trans, name, place) VALUES 
		    (scid, transid, vname, vplace)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputviewpoint(numeric, numeric, text, text)
  OWNER TO vagrant;

-- Function: inputpointcloud(numeric, numeric, text, text, text, text, text, text, numeric, numeric)

-- DROP FUNCTION inputpointcloud(numeric, numeric, text, text, text, text, text, text, numeric, numeric);

CREATE OR REPLACE FUNCTION inputpointcloud(transid numeric, vpid numeric, pfilename text, ptype text, pbboxlocal text, pbboxglobal text, pbboxpolar text, premrange text, prows numeric, pcols numeric)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO pointcloud (IDREF_trans, IDREF_view, filename, type, bbox_local, bbox_global, bbox_polar, remissionrange, rows, cols) VALUES 
		    (transid, vpid, pfilename, ptype, pbboxlocal, pbboxglobal, pbboxpolar, premrange, prows, pcols)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputpointcloud(numeric, numeric, text, text, text, text, text, text, numeric, numeric)
  OWNER TO vagrant;

-- Function: inputpanorama(numeric, numeric, text, text)

-- DROP FUNCTION inputpanorama(numeric, numeric, text, text);

CREATE OR REPLACE FUNCTION inputpanorama(transid numeric, vpid numeric, ptype text, pkindof text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO panorama (IDREF_trans, IDREF_view, type, kindof) VALUES 
		    (transid, vpid, ptype, pkindof)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputpanorama(numeric, numeric, text, text)
  OWNER TO vagrant;

-- Function: inputpanoramaimg(numeric, text, numeric)

-- DROP FUNCTION inputpanoramaimg(numeric, text, numeric);

CREATE OR REPLACE FUNCTION inputpanoramaimg(pid numeric, pimg text, porder numeric)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO panoramaimg (IDREF_pano, img, imgorder) VALUES 
		    (pid, pimg, porder)
	RETURNING id INTO id_ret; -- R𣫧abe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputpanoramaimg(numeric, text, numeric)
  OWNER TO vagrant;
  
-- Function: existendspatialcontext(text)

-- DROP FUNCTION existendspatialcontext(text);

CREATE OR REPLACE FUNCTION existendspatialcontext(namesc text)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT EXISTS(SELECT sc.name FROM spatialcontext sc WHERE name = namesc)
	--RETURNING result INTO ret; -- R𣫧abe der aktuellen ID
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION existendspatialcontext(text)
  OWNER TO vagrant;

-- Function: existendmedia(text, numeric)

-- DROP FUNCTION existendmedia(text, numeric);

CREATE OR REPLACE FUNCTION existendmedia(mediafilename text, scid numeric)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT EXISTS(SELECT m.filename FROM media m WHERE filename = mediafilename AND IDREF_sc = scid)
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION existendmedia(text, numeric)
  OWNER TO vagrant;

-- Function: existendviewpoint(text, numeric)

-- DROP FUNCTION existendviewpoint(text, numeric);

CREATE OR REPLACE FUNCTION existendviewpoint(vpname text, scid numeric)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT EXISTS(SELECT vp.name FROM viewpoint vp WHERE vp.name = vpname AND vp.IDREF_sc = scid)
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION existendviewpoint(text, numeric)
  OWNER TO vagrant;

-- Function: existendpointcloud(text, numeric)

-- DROP FUNCTION existendpointcloud(text, numeric);

CREATE OR REPLACE FUNCTION existendpointcloud(pcfilename text, vpid numeric)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT EXISTS(SELECT pc.filename FROM pointcloud pc WHERE pc.filename = pcfilename AND pc.IDREF_view = vpid)
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION existendpointcloud(text, numeric)
  OWNER TO vagrant;

-- Function: existendpanorama(text, text, numeric)

-- DROP FUNCTION existendpanorama(text, text, numeric);

CREATE OR REPLACE FUNCTION existendpanorama(ptype text, pkindof text, vpid numeric)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT EXISTS(SELECT p.type FROM panorama p WHERE p.type = ptype AND p.kindof = pkindof AND p.IDREF_view = vpid)
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION existendpanorama(text, text, numeric)
  OWNER TO vagrant;

-- Function: existendpanoramaimg(text, numeric)

-- DROP FUNCTION existendpanoramaimg(text, numeric);

CREATE OR REPLACE FUNCTION existendpanoramaimg(pimg text, pid numeric)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT EXISTS(SELECT pi.img FROM panoramaimg pi WHERE pi.img = pimg AND pi.IDREF_pano = pid)
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION existendpanoramaimg(text, numeric)
  OWNER TO vagrant;
  
-- Function: idmedia(text, numeric)

-- DROP FUNCTION idmedia(text, numeric);

CREATE OR REPLACE FUNCTION idmedia(namemed text, scid numeric)
  RETURNS integer AS
$BODY$
DECLARE ret integer;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT m.id FROM media m WHERE filename = namemed AND IDREF_sc = scid
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION idmedia(text, numeric)
  OWNER TO vagrant;
  
-- Function: idpanorama(text, text, numeric)

-- DROP FUNCTION idpanorama(text, text, numeric);

CREATE OR REPLACE FUNCTION idpanorama(ptype text, pkindof text, vpid numeric)
  RETURNS integer AS
$BODY$
DECLARE ret integer;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT p.id FROM panorama p WHERE p.type = ptype AND p.kindof = pkindof AND p.IDREF_view = vpid
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION idpanorama(text, text, numeric)
  OWNER TO vagrant;

-- Function: idpanoramaimg(text, numeric)

-- DROP FUNCTION idpanoramaimg(text, numeric);

CREATE OR REPLACE FUNCTION idpanoramaimg(pimg text, pid numeric)
  RETURNS integer AS
$BODY$
DECLARE ret integer;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT pi.id FROM panoramaimg pi WHERE pi.img = pimg AND pi.IDREF_pano = pid
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION idpanoramaimg(text, numeric)
  OWNER TO vagrant;

-- Function: idpointcloud(text, numeric)

-- DROP FUNCTION idpointcloud(text, numeric);

CREATE OR REPLACE FUNCTION idpointcloud(pcfilename text, vpid numeric)
  RETURNS integer AS
$BODY$
DECLARE ret integer;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT pc.id FROM pointcloud pc WHERE pc.filename = pcfilename AND IDREF_view = vpid
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION idpointcloud(text, numeric)
  OWNER TO vagrant;

-- Function: idspatialcontext(text)

-- DROP FUNCTION idspatialcontext(text);

CREATE OR REPLACE FUNCTION idspatialcontext(namesc text)
  RETURNS integer AS
$BODY$
DECLARE ret integer;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT sc.id FROM spatialcontext sc WHERE name = namesc
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION idspatialcontext(text)
  OWNER TO vagrant;
  
-- Function: idviewpoint(text, numeric)

-- DROP FUNCTION idviewpoint(text, numeric);

CREATE OR REPLACE FUNCTION idviewpoint(vpname text, scid numeric)
  RETURNS integer AS
$BODY$
DECLARE ret integer;

BEGIN 
	SELECT * INTO ret FROM
	(
		SELECT vp.id FROM viewpoint vp WHERE vp.name = vpname AND IDREF_sc = scid
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION idviewpoint(text, numeric)
  OWNER TO vagrant;
	
-- Delete

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
	-- Datenbanktabellen l򳣨en --
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
  OWNER TO vagrant;

-- Function: cleardatabase(text)

-- DROP FUNCTION cleardatabase(text);

CREATE OR REPLACE FUNCTION cleardatabase(spatialcon text)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	-- Datenbanktabellen l򳣨en --
	DELETE FROM spatialcontext s WHERE s.name = spatialcon;

	RETURN true;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION cleardatabase(text)
  OWNER TO vagrant;

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
  OWNER TO vagrant;

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
  OWNER TO vagrant;
	
	
	