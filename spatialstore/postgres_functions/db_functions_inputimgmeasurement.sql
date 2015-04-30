-----------------------------
-- Input Image Measurement --
-----------------------------

-- Function: inputfeature(text, text)

-- DROP FUNCTION inputfeature(text, text);

CREATE OR REPLACE FUNCTION inputfeature(spatialcon text, featuretype text)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO feature (IDREF_sc, type) VALUES 
	(
	(SELECT sc.id FROM spatialcontext sc WHERE sc.name = spatialcon LIMIT 1), -- sc id
	featuretype -- featuretype
	)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputfeature(text, text)
  OWNER TO postgres;

-- Function: inputfeatureviewpoint(text, text, integer)

-- DROP FUNCTION inputfeatureviewpoint(text, text, integer);

CREATE OR REPLACE FUNCTION inputfeatureviewpoint(spatialcon text, viewpoint text, featureid integer)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO feature_viewpoint (IDREF_feature, IDREF_view) VALUES 
	(
	featureID, -- feature ID
	(SELECT v.id FROM viewpoint v, spatialcontext sc WHERE v.name = viewpoint AND v.IDREF_sc = sc.id AND sc.name = spatialcon) -- vp id
	)
	RETURNING feature_viewpoint.IDREF_feature INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputfeatureviewpoint(text, text, integer)
  OWNER TO postgres;

-- Function: inputimgmeasurement(numeric, numeric, text, numeric, numeric, text)

-- DROP FUNCTION inputimgmeasurement(numeric, numeric, text, numeric, numeric, text);

CREATE OR REPLACE FUNCTION inputimgmeasurement(pano numeric, feat numeric, creator text, zoom numeric, resolution numeric, type text, dt timestamp)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO imgmeasurement (IDREF_pano, IDREF_feature, creator, zoom, resolution, type, date) VALUES 
		(pano, feat, creator, zoom, resolution, type, dt)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputimgmeasurement(numeric, numeric, text, numeric, numeric, text, timestamp)
  OWNER TO postgres;

-- Function: inputimgmeasurementpoint(numeric, numeric, numeric, numeric)

-- DROP FUNCTION inputimgmeasurementpoint(numeric, numeric, numeric, numeric);

CREATE OR REPLACE FUNCTION inputimgmeasurementpoint(imgmeas numeric, theta numeric, phi numeric, pointorder numeric)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO imgmeasurement_point (IDREF_imgmeas, theta, phi, pointorder) VALUES 
		    (imgmeas, theta, phi, pointorder)
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputimgmeasurementpoint(numeric, numeric, numeric, numeric)
  OWNER TO postgres;

-- Function: featureviewpointexists(text, text, integer)

-- DROP FUNCTION featureviewpointexists(text, text, integer);

CREATE OR REPLACE FUNCTION featureviewpointexists(spatialcon text, viewp text, featureid integer)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 

	SELECT * INTO id_ret FROM 

	(

		SELECT fv.IDREF_feature
		FROM viewpoint v, feature_viewpoint fv, spatialcontext sc
		WHERE fv.IDREF_feature = featureid 
			AND fv.IDREF_view = (SELECT v.id FROM viewpoint v WHERE v.name = viewp AND v.IDREF_sc = sc.id AND sc.name = spatialcon)
		LIMIT 1

	) AS result;

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION featureviewpointexists(text, text, integer)
  OWNER TO postgres;
  
-- Function: inputfeaturepoint(text, text, text, text, integer)

-- DROP FUNCTION inputfeaturepoint(text, text, text, text, integer);

CREATE OR REPLACE FUNCTION inputfeaturepoint(_x text, _y text, _z text, _i text, srid integer, fid integer, porder integer)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO featurepoint (geom, idref_feature, pointorder) VALUES 
		( ST_GeomFromText('POINT('||_x||' '||_y||' '||_z||' '||_i||')', srid), fid, porder )
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputfeaturepoint(text, text, text, text, integer, integer, integer)
  OWNER TO i3admin;