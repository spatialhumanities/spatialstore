----------------
-- Pointcloud --
----------------

-- Function: getpointclouddata(text, text)

-- DROP FUNCTION getpointclouddata(text, text);

CREATE OR REPLACE FUNCTION getpointclouddata(spatialcon text, vpoint text)
  RETURNS text AS
$BODY$

DECLARE
filename_ TEXT;
rows_ integer;
cols_ integer;
transformation_ TEXT;

BEGIN 
	SELECT INTO filename_, rows_, cols_, transformation_
		pc.filename, pc.rows, pc.cols, t.param
		FROM pointcloud pc , spatialcontext sc , viewpoint vp, transformation t
		WHERE sc.name = spatialcon AND sc.id = vp.IDREF_sc AND vp.name = vpoint AND vp.id = pc.IDREF_view AND t.id = pc.IDREF_trans;

	RETURN filename_ || '###' || rows_ || '###'|| cols_ || '###' || transformation_;
	
END; $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getpointclouddata(text, text)
  OWNER TO postgres;

-- Function: getstrans(text)

-- DROP FUNCTION getstrans(text);

CREATE OR REPLACE FUNCTION getstrans(spatialcon text)
  RETURNS text AS
$BODY$
DECLARE trans_ text;

BEGIN 

	SELECT * INTO trans_ FROM 

	(

		select t.param
		from spatialcontext sc, transformation t
		where sc.name = spatialcon and sc.IDREF_trans = t.id

	) AS result;

    RETURN trans_;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getstrans(text)
  OWNER TO postgres;

-- Function: getvtrans(text, text)

-- DROP FUNCTION getvtrans(text, text);

CREATE OR REPLACE FUNCTION getvtrans(spatialcon text, viewp text)
  RETURNS text AS
$BODY$
DECLARE trans_ text;

BEGIN 

	SELECT * INTO trans_ FROM 

	(

		select t.param
		from spatialcontext sc, viewpoint v, transformation t
		where sc.name = spatialcon and v.IDREF_sc = sc.id and v.name = viewp and v.IDREF_trans = t.id

	) AS result;

    RETURN trans_;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getvtrans(text, text)
  OWNER TO postgres;

-- Function: inputfeaturepoint(text, text, text, text, integer)

-- DROP FUNCTION inputfeaturepoint(text, text, text, text, integer);

CREATE OR REPLACE FUNCTION inputfeaturepoint(_x text, _y text, _z text, _i text, srid integer)
  RETURNS integer AS
$BODY$
DECLARE id_ret integer;

BEGIN 
	INSERT INTO featurepoint (geom) VALUES 
		( ST_GeomFromText('POINT('||_x||' '||_y||' '||_z||' '||_i||')', srid) )
	RETURNING id INTO id_ret; -- Rückgabe der aktuellen ID

    RETURN id_ret;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION inputfeaturepoint(text, text, text, text, integer)
  OWNER TO postgres;