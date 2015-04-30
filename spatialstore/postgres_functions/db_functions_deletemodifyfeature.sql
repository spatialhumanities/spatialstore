-----------------------------
-- Delete / Modify Feature --
-----------------------------

-- Function: deletefeature(numeric)

-- DROP FUNCTION deletefeature(numeric);

CREATE OR REPLACE FUNCTION deletefeature(feat numeric)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;

BEGIN 
	-- Datenbankeintrag in Feature Tabelle löschen --
	-- On delete cascade feature_viewpoint, featurepoint, imgmeasurement, imgmeasurement_point
	DELETE FROM feature WHERE feature.id = feat;

	RETURN true;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION deletefeature(numeric)
  OWNER TO postgres;
  
-- Function: updatefeaturevisibility(integer, text)

-- DROP FUNCTION updatefeaturevisibility(integer, text);

CREATE OR REPLACE FUNCTION updatefeaturevisibility(feat integer, vname text)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;
DECLARE vid integer;

BEGIN 

	-- Get ViewpointID from ViewpointID and FeatureID
	SELECT * INTO vid FROM
	( 
		SELECT v.id
		FROM viewpoint AS v, feature AS f
		WHERE v.name = vname AND v.idref_sc = f.idref_sc AND f.id = feat
		
	) AS result;

	IF EXISTS (
		SELECT fv.idref_view
		FROM feature_viewpoint AS fv
		WHERE fv.idref_feature = feat AND fv.idref_view = vid
	) THEN 
		ret = false;
	ELSE
		-- Datenbankeintrag in Feature_Viewpoint Tabelle updaten --
		ret = true;
		INSERT INTO feature_viewpoint VALUES (feat,vid);
	END IF;

	RETURN ret;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION updatefeaturevisibility(integer, text)
  OWNER TO postgres;


-- Function: deletefeaturevisibility(integer, text)

-- DROP FUNCTION deletefeaturevisibility(integer, text);

CREATE OR REPLACE FUNCTION deletefeaturevisibility(feat integer, vname text)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;
DECLARE vid integer;

BEGIN 
	-- Get ViewpointID from ViewpointID and FeatureID
	SELECT * INTO vid FROM
	( 
		SELECT v.id
		FROM viewpoint AS v, feature AS f
		WHERE v.name = vname AND v.idref_sc = f.idref_sc AND f.id = feat
		
	) AS result;
	
	-- Datenbankeintrag in Feature_Viewpoint Tabelle updaten --
	INSERT INTO feature_viewpoint VALUES (feat,vid);
	DELETE FROM feature_viewpoint WHERE feature_viewpoint.idref_feature = feat AND feature_viewpoint.idref_view = vid;

	RETURN true;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION deletefeaturevisibility(integer, text)
  OWNER TO postgres;

-- Function: deleteimgmeasurementandpoint(integer)

-- DROP FUNCTION deleteimgmeasurementandpoint(integer);

CREATE OR REPLACE FUNCTION deleteimgmeasurementandpoint(feat integer)
  RETURNS boolean AS
$BODY$
DECLARE ret boolean;
DECLARE vid integer;

BEGIN 

	-- Datenbankeintrag in Feature_Viewpoint Tabelle updaten --
	DELETE FROM imgmeasurement WHERE imgmeasurement.idref_feature = feat;

	RETURN true;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION deleteimgmeasurementandpoint(integer)
  OWNER TO postgres;

-- Function: deletefeaturepoint(integer)//not used table was removed from  the database

-- DROP FUNCTION deletefeaturepoint(integer);

--CREATE OR REPLACE FUNCTION deletefeaturepoint(feat integer)
 -- RETURNS boolean AS
--$BODY$
--DECLARE ret boolean;
--DECLARE vid integer;

--BEGIN 

	-- Datenbankeintrag in Feature_Viewpoint Tabelle updaten --
	--DELETE FROM featurepoint WHERE featurepoint.idref_feature = feat;

	--RETURN true;
--END;

--$BODY$
 -- LANGUAGE plpgsql VOLATILE
 -- COST 100;
--ALTER FUNCTION deletefeaturepoint(integer)
  --OWNER TO postgres;
