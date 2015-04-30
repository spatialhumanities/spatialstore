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
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputtransformation(text, text, text)
  OWNER TO postgres;
  
-- Function: inputspatialcontext(numeric, text, text, date)

-- DROP FUNCTION inputspatialcontext(numeric, text, text, date);

CREATE OR REPLACE FUNCTION inputspatialcontext(transid numeric, namesc text, placesc text, datesc date)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO spatialcontext (IDREF_trans, name, place, date) VALUES 
		    (transid, namesc, placesc, datesc)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputspatialcontext(numeric, text, text, date)
  OWNER TO postgres;
  
CREATE OR REPLACE FUNCTION inputspatialcontext(transid numeric, namesc text, placesc text, datesc date, sridsc numeric)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO spatialcontext (IDREF_trans, name, place, date, srid) VALUES 
		    (transid, namesc, placesc, datesc, sridsc)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputspatialcontext(numeric, text, text, date, numeric)
  OWNER TO postgres;


-- Function: inputmedia(numeric, numeric, text, text, text)

-- DROP FUNCTION inputmedia(numeric, numeric, text, text, text);

CREATE OR REPLACE FUNCTION inputmedia(transid numeric, scid numeric, mtype text, mfilename text, mdesc text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO media (IDREF_sc, IDREF_trans, filename, type, description) VALUES 
		    (scid, transid, mfilename, mtype, mdesc)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputmedia(numeric, numeric, text, text, text)
  OWNER TO postgres;

-- Function: inputviewpoint(numeric, numeric, text, text)

-- DROP FUNCTION inputviewpoint(numeric, numeric, text, text);

CREATE OR REPLACE FUNCTION inputviewpoint(transid numeric, scid numeric, vname text, vplace text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO viewpoint (IDREF_sc, IDREF_trans, name, place) VALUES 
		    (scid, transid, vname, vplace)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputviewpoint(numeric, numeric, text, text)
  OWNER TO postgres;

-- Function: inputpointcloud(numeric, numeric, text, text, text, text, text, text, numeric, numeric)

-- DROP FUNCTION inputpointcloud(numeric, numeric, text, text, text, text, text, text, numeric, numeric);

CREATE OR REPLACE FUNCTION inputpointcloud(transid numeric, vpid numeric, pfilename text, ptype text, pbboxlocal text, pbboxglobal text, pbboxpolar text, premrange text, prows numeric, pcols numeric)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO pointcloud (IDREF_trans, IDREF_view, filename, type, bbox_local, bbox_global, bbox_polar, remissionrange, rows, cols) VALUES 
		    (transid, vpid, pfilename, ptype, pbboxlocal, pbboxglobal, pbboxpolar, premrange, prows, pcols)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputpointcloud(numeric, numeric, text, text, text, text, text, text, numeric, numeric)
  OWNER TO postgres;

-- Function: inputpanorama(numeric, numeric, text, text)

-- DROP FUNCTION inputpanorama(numeric, numeric, text, text);

CREATE OR REPLACE FUNCTION inputpanorama(transid numeric, vpid numeric, ptype text, pkindof text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO panorama (IDREF_trans, IDREF_view, type, kindof) VALUES 
		    (transid, vpid, ptype, pkindof)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputpanorama(numeric, numeric, text, text)
  OWNER TO postgres;

-- Function: inputpanoramaimg(numeric, text, numeric)

-- DROP FUNCTION inputpanoramaimg(numeric, text, numeric);

CREATE OR REPLACE FUNCTION inputpanoramaimg(pid numeric, pimg text, porder numeric)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO panoramaimg (IDREF_pano, img, imgorder) VALUES 
		    (pid, pimg, porder)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputpanoramaimg(numeric, text, numeric)
  OWNER TO postgres;
  
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
	--RETURNING result INTO ret; -- Rückgabe der aktuellen ID
	) AS result;

    RETURN ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION existendspatialcontext(text)
  OWNER TO i3admin;

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
  OWNER TO postgres;

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
  OWNER TO i3admin;

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
  OWNER TO postgres;

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
  OWNER TO postgres;

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
  OWNER TO postgres;
  
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
  OWNER TO postgres;
  
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
  OWNER TO postgres;

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
  OWNER TO postgres;

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
  OWNER TO postgres;

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
  OWNER TO postgres;
  
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
  OWNER TO postgres;
	

	

	
	
	
	