--------------------
-- ReST Interface --
--------------------

-- Function: getspatialcontexts()

-- DROP FUNCTION getspatialcontexts();

CREATE OR REPLACE FUNCTION getspatialcontexts()
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 

		(

		SELECT
			XMLFOREST(result."xml-spatialcontext" AS "spatialcontexts")
		FROM
		(
			SELECT
				XMLAGG(
					xmlelement(name spatialcontext, xmlattributes(sc.name as "id",'http://s.spatialhumanities.de/' || sc.name as "href")) ORDER BY sc.name ASC
				)
				AS "xml-spatialcontext" 
			FROM
				spatialcontext sc
			) AS result
		) 

	AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getspatialcontexts()
  OWNER TO postgres;

-- Function: getviewpoints(text)

-- DROP FUNCTION getviewpoints(text);

CREATE OR REPLACE FUNCTION getviewpoints(spatialcon text)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 

		(

		SELECT
	xmlconcat(
		xmlelement(name spatialcontext, xmlattributes(resultName."Name" as id,'http://s.spatialhumanities.de/' || resultName."Name" as "href"), 
			xmlelement(name resource, xmlattributes('viewpoints' as id, 'http://s.spatialhumanities.de/' || resultName."Name" || '/viewpoints' as "href"), 
				XMLFOREST(resultViewpoint."xml-viewpoints" AS "elements") 
			)
		)	
	)
FROM
	(
		SELECT
			XMLAGG(
				xmlelement(name viewpoint, xmlattributes(vp.name as "id",'http://s.spatialhumanities.de/' || sc.name || '/viewpoints/' || vp.name as "href")) ORDER BY vp.name ASC
			)
			AS "xml-viewpoints"
		FROM
			viewpoint vp, spatialcontext sc
		WHERE
			sc.name = spatialcon AND vp.IDREF_sc = sc.id
		LIMIT 1
	) AS resultViewpoint,
	(
		SELECT
			sc.name AS "Name"
		FROM
			viewpoint vp, spatialcontext sc
		WHERE
			sc.name = spatialcon AND vp.IDREF_sc = sc.id
		LIMIT 1
	) AS resultName
		
		) 

	AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewpoints(text)
  OWNER TO postgres;

-- Function: getfeatures(text)

-- DROP FUNCTION getfeatures(text);

CREATE OR REPLACE FUNCTION getfeatures(spatialcon text)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(

		SELECT
	xmlconcat(
		xmlelement(name spatialcontext, xmlattributes(resultName."Name" as id,'http://s.spatialhumanities.de/' || resultName."Name" as "href"),
			xmlelement(name resource, xmlattributes('features' as id, 'http://s.spatialhumanities.de/' || resultName."Name" || '/features' as "href"),  
				XMLFOREST(resultFeature."xml-features" AS "elements") 
			)
		)	
	)
FROM
	(
		SELECT
			XMLAGG(
				xmlelement(name feature, xmlattributes(f.id as "id",'http://s.spatialhumanities.de/' || sc.name || '/features/' || f.id as "href")) ORDER BY f.id ASC
			)
			AS "xml-features"
		FROM
			feature f, spatialcontext sc
		WHERE
			sc.name = spatialcon AND f.IDREF_sc = sc.id
		LIMIT 1
	) AS resultFeature,
	(
		SELECT
			sc.name AS "Name"
		FROM
			feature f, spatialcontext sc
		WHERE
			sc.name = spatialcon AND f.IDREF_sc = sc.id
		LIMIT 1
	) AS resultName 
	
	)

	AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getfeatures(text)
  OWNER TO postgres;

-- Function: getfeaturegeometry(integer)

-- DROP FUNCTION getfeaturegeometry(integer);

CREATE OR REPLACE FUNCTION getfeaturegeometry(fid integer)
  RETURNS text AS
$BODY$
DECLARE geom_ text;

BEGIN 

	SELECT * INTO geom_ FROM 

	(

		select st_asewkt(featurepoint.geom)
		from featurepoint
		where featurepoint.idref_feature = fid

	) AS result;

    RETURN geom_;
	
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getfeaturegeometry(integer)
  OWNER TO postgres;

-- Function: getscid(text)

-- DROP FUNCTION getscid(text);

CREATE OR REPLACE FUNCTION getscid(spatialcon text)
  RETURNS integer AS
$BODY$
DECLARE intOut int;

BEGIN 
	SELECT * INTO intOut FROM 

	(
		SELECT sc.id FROM spatialcontext AS sc WHERE sc.name = spatialcon
	)

	AS intSelect;

	RETURN intOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getscid(text)
  OWNER TO postgres;
  
-- Function: getvpid(text)

-- DROP FUNCTION getvpid(text);

CREATE OR REPLACE FUNCTION getvpid(vp text)
  RETURNS integer AS
$BODY$
DECLARE intOut int;

BEGIN 
	SELECT * INTO intOut FROM 

	(
		SELECT v.id FROM viewpoint AS v WHERE v.name = vp
	)

	AS intSelect;

	RETURN intOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getvpid(text)
  OWNER TO postgres;


-- Function: getmetadata(text)

-- DROP FUNCTION getmetadata(text);

CREATE OR REPLACE FUNCTION getmetadata(spatialcon text)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 

		(

		SELECT 
			xmlconcat(
				xmlelement(name spatialcontext, xmlattributes(sc.name as id,'http://s.spatialhumanities.de/' || sc.name as "href"),
				xmlelement(name resource, xmlattributes('metadata' as id, 'http://s.spatialhumanities.de/' || sc.name || '/metadata' as "href"),  
				xmlelement(name elements, 
				xmlelement(name spatialcontext, 
				xmlforest(sc.place AS place, sc.date AS date)
			),
			xmlelement(name transformation, 
				xmlforest(t.srccrs AS scrCRS, t.dstcrs AS dstCRS, t.param AS params)
			)
		)
		)
	)
)
            
		FROM spatialcontext AS sc, transformation AS t
		WHERE sc.IDREF_trans = t.id AND sc.name = spatialcon
		
		) 

	AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getmetadata(text)
  OWNER TO postgres;

-- Function: getmedia(text)

-- DROP FUNCTION getmedia(text);

CREATE OR REPLACE FUNCTION getmedia(spatialcon text)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(
	
		SELECT
			xmlconcat(
				xmlelement(name spatialcontext, xmlattributes(resultName."Name" as id,'http://s.spatialhumanities.de/' || resultName."Name" as "href"),
					xmlelement(name resource, xmlattributes('media' as id, 'http://s.spatialhumanities.de/' || resultName."Name" || '/media' as "href"),   
						XMLFOREST(resultViewpoint."xml-media" AS "elements") 
					)
				)	
		)
FROM
	(
		SELECT
			XMLAGG(
				xmlconcat(
					xmlelement(name media, xmlattributes(m.id as "id"),

						xmlelement(name data, 
						xmlforest(m.type AS mediatype, m.filename AS filename)),

						xmlelement(name transformation, 
						xmlforest(t.srccrs AS srccrs, t.dstcrs AS dstcrs, t.param AS params))
					)
				)
			ORDER BY m.type ASC
			) 
			
			AS "xml-media"
		FROM
			spatialcontext sc, media m, transformation t
		WHERE
			sc.name = spatialcon AND m.IDREF_sc = sc.id AND sc.IDREF_trans = t.id
	) AS resultViewpoint,
	
	(
		SELECT
			sc.name AS "Name"
		FROM
			viewpoint vp, spatialcontext sc
		WHERE
			sc.name = spatialcon AND vp.IDREF_sc = sc.id
		LIMIT 1
	) AS resultName
	
	)

	AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getmedia(text)
  OWNER TO postgres;

-- Function: getviewpointmetadata(text, text)

-- DROP FUNCTION getviewpointmetadata(text, text);

CREATE OR REPLACE FUNCTION getviewpointmetadata(spatialcon text, vpoint text)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(
	
		SELECT
		XMLAGG(
			xmlconcat(
				xmlelement(name metadata,

					xmlforest(vp.place AS name),

					xmlelement(name transformation, 
						xmlforest(t.srccrs AS srccrs, t.dstcrs AS dstcrs, t.param AS params)
					)
				)
			)
		) AS "xml"
		FROM
			spatialcontext sc, viewpoint vp, transformation t
		WHERE
			sc.name = spatialcon AND vp.name = vpoint AND vp.IDREF_sc = sc.id AND vp.IDREF_trans = t.id
		
	) AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewpointmetadata(text, text)
  OWNER TO postgres;

-- Function: getviewpointpanoramametadata(text, text, numeric)

-- DROP FUNCTION getviewpointpanoramametadata(text, text, numeric);

CREATE OR REPLACE FUNCTION getviewpointpanoramametadata(spatialcon text, vpoint text, pid numeric)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(
	
		SELECT
			XMLAGG(
				xmlconcat(
					xmlforest(p.type AS structuraltype, p.kindof AS kindof),

				xmlelement(name transformation, 
				xmlforest(t.srccrs AS srccrs, t.dstcrs AS dstcrs, t.param AS params)
				)	
				)
			) AS "xml"
		FROM
			spatialcontext sc, viewpoint vp, transformation t, panorama p
		WHERE
	sc.name = spatialcon AND vp.name = vpoint AND vp.IDREF_sc = sc.id AND p.IDREF_trans = t.id AND p.id = pid
		
	) AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewpointpanoramametadata(text, text, numeric)
  OWNER TO postgres;

-- Function: getviewpointpanoramaimages(text, text, numeric)

-- DROP FUNCTION getviewpointpanoramaimages(text, text, numeric);

CREATE OR REPLACE FUNCTION getviewpointpanoramaimages(spatialcon text, vpoint text, pid numeric)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(
	
		SELECT
			XMLFOREST(resultImages."xml" AS "images") 
		FROM
		(
		SELECT
			XMLAGG(
				xmlconcat(
					xmlforest(pi.img AS img)
				) ORDER BY pi.imgorder ASC
			) AS "xml"
		FROM
			spatialcontext sc, viewpoint vp, transformation t, panorama p, panoramaimg pi
		WHERE
			sc.name = spatialcon AND vp.name = vpoint AND vp.IDREF_sc = sc.id AND vp.IDREF_trans = t.id AND p.id = pid AND pi.IDREF_pano = p.id
		) AS resultImages
		
	) AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewpointpanoramaimages(text, text, numeric)
  OWNER TO postgres;

-- Function: getviewpointmeasurementmetadata(text, text, numeric)

-- DROP FUNCTION getviewpointmeasurementmetadata(text, text, numeric);

CREATE OR REPLACE FUNCTION getviewpointmeasurementmetadata(spatialcon text, vpoint text, mid numeric)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(
	
		SELECT
			XMLAGG(
				xmlconcat(
					xmlforest(im.creator AS creator, im.date AS date, im.zoom AS zoom, im.resolution AS resolution, im.type AS geomtype, im.IDREF_pano AS panorama, im.IDREF_feature AS featureid)
				) 
			) AS "xml"
		FROM
			spatialcontext sc, viewpoint vp, panorama p, imgmeasurement im
		WHERE
			sc.name = spatialcon AND vp.name = vpoint AND im.id = mid AND vp.IDREF_sc = sc.id AND p.IDREF_view = vp.id AND im.IDREF_pano = p.id
	) AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewpointmeasurementmetadata(text, text, numeric)
  OWNER TO postgres;

-- Function: getviewpointmeasurementpoints(text, text, numeric)

-- DROP FUNCTION getviewpointmeasurementpoints(text, text, numeric);

CREATE OR REPLACE FUNCTION getviewpointmeasurementpoints(spatialcon text, vpoint text, mid numeric)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(
	
		SELECT
			xmlconcat(
				XMLFOREST(resultPoint."xml" AS "points") 
			)
		FROM
		(		
			SELECT
				XMLAGG(
					xmlconcat(
						xmlelement(name point, 
							xmlforest(ip.theta AS theta, ip.phi AS phi)
						)
					) ORDER BY ip.pointorder ASC
				) AS "xml"
			FROM
				spatialcontext sc, viewpoint vp, panorama p, imgmeasurement im, imgmeasurement_point ip
			WHERE
				sc.name = spatialcon AND vp.name = vpoint AND im.id = mid AND vp.IDREF_sc = sc.id AND p.IDREF_view = vp.id AND im.IDREF_pano = p.id AND ip.IDREF_imgmeas = im.id
		) AS resultPoint

	) AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewpointmeasurementpoints(text, text, numeric)
  OWNER TO postgres;

-- Function: getviewerdata(text)

-- DROP FUNCTION getviewerdata(text);

CREATE OR REPLACE FUNCTION getviewerdata(spatialcon text)
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 
	
	(
	
		SELECT
			xmlconcat(
				xmlelement(name spatialcontext, xmlattributes(resultName."Name" as id), 
					xmlelement(name resource, xmlattributes('viewerdata' as id), 
						xmlelement(name elements,
							XMLFOREST(resultViewpoint."xml" AS "viewpoints",
								resultGroundplan."xml" AS "groundplans",
										resultFeature."xml" AS "features") 
						)
					)
				)	
			)
		FROM
		(
			SELECT
				XMLAGG(
					xmlconcat(
						xmlelement(name viewpoint, xmlattributes(vp.name as "id"),
						xmlforest(vp.place AS name),
						xmlelement(name transformation, 
					xmlforest(t.srccrs AS srccrs, t.dstcrs AS dstcrs, t.param AS params)
						)                           
						)
					)
			        )  
			AS "xml"
		FROM
			viewpoint vp, spatialcontext sc, transformation t
		WHERE
			sc.name = spatialcon AND vp.IDREF_sc = sc.id AND vp.IDREF_trans = t.id
		LIMIT 1
	) AS resultViewpoint,
	(
		SELECT
			XMLAGG(
				xmlconcat(
					xmlelement(name grundplan, xmlattributes(m.id as "id"),
					xmlelement(name data, 
				xmlforest(m.filename AS url, m.description AS description)
				),
					xmlelement(name transformation, 
				xmlforest(t.srccrs AS srccrs, t.dstcrs AS dstcrs, t.param AS params)
				)
				))
			)
			AS "xml"
		FROM
			spatialcontext sc, transformation t, media m
		WHERE
			sc.name = spatialcon AND m.IDREF_sc = sc.id AND m.IDREF_trans = t.id AND m.type = 'groundplan'
		LIMIT 1
	) AS resultGroundplan,
	(
		SELECT
			XMLAGG(
				xmlconcat(
					xmlelement(name feature, xmlattributes(f.id as "id"))
				)
			)
			AS "xml"
		FROM
			spatialcontext sc, feature f
		WHERE
			sc.name = spatialcon AND f.IDREF_sc = sc.id
		LIMIT 1
	) AS resultFeature,
	(
		SELECT
			sc.name AS "Name"
		FROM
			viewpoint vp, spatialcontext sc
		WHERE
			sc.name = spatialcon AND vp.IDREF_sc = sc.id
		LIMIT 1
	) AS resultName
	
	)

	AS xmlSelect;

	RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewerdata(text)
  OWNER TO postgres;

-- Function: getviewerdatasc()

-- DROP FUNCTION getviewerdatasc();

CREATE OR REPLACE FUNCTION getviewerdatasc()
  RETURNS xml AS
$BODY$
DECLARE xmlOut xml;

BEGIN 
	SELECT * INTO xmlOut FROM 

	(

		SELECT
	xmlconcat(
		XMLFOREST(result."xml" AS "elements") 	
	)
FROM
(
	SELECT
		XMLAGG(
			xmlconcat(
				xmlelement(name spatialcontext, xmlattributes(sc.name as "id"),
					xmlforest(sc.place AS place, sc.date AS date)
				)
			)
		) AS "xml"
	FROM
		spatialcontext sc
) AS result
		
	) AS xmlSelect;

RETURN xmlOut;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION getviewerdatasc()
  OWNER TO postgres;