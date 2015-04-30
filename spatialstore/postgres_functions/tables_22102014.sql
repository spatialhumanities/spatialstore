-- Extension: postgis
CREATE EXTENSION IF NOT EXISTS postgis;

-- Table: transformation

-- DROP TABLE transformation;

CREATE TABLE transformation
(
  id serial NOT NULL,
  srccrs character varying,
  dstcrs character varying,
  param character varying,
  CONSTRAINT transformation_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE transformation
  OWNER TO vagrant;
  
-- Table: spatialcontext
  
-- DROP TABLE spatialcontext;

CREATE TABLE spatialcontext
(
  id serial NOT NULL,
  idref_trans integer,
  name character varying,
  place character varying,
  date date,
  srid integer,
  CONSTRAINT spatialcontext_pkey PRIMARY KEY (id),
  CONSTRAINT spatialcontext_idref_trans_fkey FOREIGN KEY (idref_trans)
      REFERENCES transformation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE spatialcontext
  OWNER TO vagrant;

-- Table: media
  
-- DROP TABLE media;

CREATE TABLE media
(
  id serial NOT NULL,
  idref_sc integer,
  idref_trans integer,
  filename character varying,
  type character varying,
  description character varying,
  CONSTRAINT media_pkey PRIMARY KEY (id),
  CONSTRAINT media_idref_sc_fkey FOREIGN KEY (idref_sc)
      REFERENCES spatialcontext (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT media_idref_trans_fkey FOREIGN KEY (idref_trans)
      REFERENCES transformation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE media
  OWNER TO vagrant;
  
-- Table: viewpoint

-- DROP TABLE viewpoint;

CREATE TABLE viewpoint
(
  id serial NOT NULL,
  idref_sc integer,
  idref_trans integer,
  name character varying,
  place character varying,
  CONSTRAINT viewpoint_pkey PRIMARY KEY (id),
  CONSTRAINT viewpoint_idref_sc_fkey FOREIGN KEY (idref_sc)
      REFERENCES spatialcontext (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT viewpoint_idref_trans_fkey FOREIGN KEY (idref_trans)
      REFERENCES transformation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE viewpoint
  OWNER TO vagrant;  

-- Table: pointcloud

-- DROP TABLE pointcloud;

CREATE TABLE pointcloud
(
  id serial NOT NULL,
  idref_trans integer,
  idref_view integer,
  filename character varying,
  type character varying,
  bbox_local character varying,
  bbox_global character varying,
  bbox_polar character varying,
  remissionrange character varying,
  rows integer,
  cols integer,
  CONSTRAINT pointcloud_pkey PRIMARY KEY (id),
  CONSTRAINT pointcloud_idref_trans_fkey FOREIGN KEY (idref_trans)
      REFERENCES transformation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT pointcloud_idref_view_fkey FOREIGN KEY (idref_view)
      REFERENCES viewpoint (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE pointcloud
  OWNER TO vagrant;

-- Table: panorama

-- DROP TABLE panorama;

CREATE TABLE panorama
(
  id serial NOT NULL,
  idref_view integer,
  idref_trans integer,
  type character varying,
  kindof character varying,
  CONSTRAINT panorama_pkey PRIMARY KEY (id),
  CONSTRAINT panorama_idref_trans_fkey FOREIGN KEY (idref_trans)
      REFERENCES transformation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT panorama_idref_view_fkey FOREIGN KEY (idref_view)
      REFERENCES viewpoint (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE panorama
  OWNER TO vagrant;

-- Table: panoramaimg

-- DROP TABLE panoramaimg;

CREATE TABLE panoramaimg
(
  id serial NOT NULL,
  idref_pano integer,
  img character varying,
  imgorder integer,
  CONSTRAINT panoramaimg_pkey PRIMARY KEY (id),
  CONSTRAINT panoramaimg_idref_pano_fkey FOREIGN KEY (idref_pano)
      REFERENCES panorama (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE panoramaimg
  OWNER TO vagrant;

-- Table: feature

-- DROP TABLE feature;

CREATE TABLE feature
(
  id serial NOT NULL,
  idref_sc integer,
  geom geometry,
  CONSTRAINT feature_pkey PRIMARY KEY (id),
  CONSTRAINT feature_idref_sc_fkey FOREIGN KEY (idref_sc)
      REFERENCES spatialcontext (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE feature
  OWNER TO vagrant;
  
-- Table: edit

-- DROP TABLE edit;

CREATE TABLE edit
(
  id serial NOT NULL,
  idref_feature integer,
  creator character varying,
  date timestamp without time zone,
  CONSTRAINT edit_pkey PRIMARY KEY (id),
  CONSTRAINT edit_idref_feature_fkey FOREIGN KEY (idref_feature)
      REFERENCES feature (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE edit
  OWNER TO vagrant;

-- Table: import

-- DROP TABLE import;

CREATE TABLE import
(
  id serial NOT NULL,
  idref_edit integer,
  license character varying,
  CONSTRAINT import_pkey PRIMARY KEY (id),
  CONSTRAINT import_idref_edit_fkey FOREIGN KEY (idref_edit)
      REFERENCES edit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE import
  OWNER TO vagrant;

-- Table: imgmeasurement

-- DROP TABLE imgmeasurement;

CREATE TABLE imgmeasurement
(
  id serial NOT NULL,
  idref_pano integer,
  idref_edit integer,
  zoom double precision,
  resolution double precision,
  geomtype character varying,
  CONSTRAINT imgmeasurement_pkey PRIMARY KEY (id),
  CONSTRAINT imgmeasurement_idref_edit_fkey FOREIGN KEY (idref_edit)
      REFERENCES edit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT imgmeasurement_idref_pano_fkey FOREIGN KEY (idref_pano)
      REFERENCES panorama (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE imgmeasurement
  OWNER TO vagrant;


-- Table: imgmeasurement_point

-- DROP TABLE imgmeasurement_point;

CREATE TABLE imgmeasurement_point
(
  id serial NOT NULL,
  idref_imgmeas integer,
  theta double precision,
  phi double precision,
  pointorder integer,
  CONSTRAINT imgmeasurement_point_pkey PRIMARY KEY (id),
  CONSTRAINT imgmeasurement_point_idref_imgmeas_fkey FOREIGN KEY (idref_imgmeas)
      REFERENCES imgmeasurement (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE imgmeasurement_point
  OWNER TO vagrant;

-- Table: feature_viewpoint

-- DROP TABLE feature_viewpoint;

CREATE TABLE feature_viewpoint
(
  idref_feature integer,
  idref_view integer,
  CONSTRAINT feature_viewpoint_idref_feature_fkey FOREIGN KEY (idref_feature)
      REFERENCES feature (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT feature_viewpoint_idref_view_fkey FOREIGN KEY (idref_view)
      REFERENCES viewpoint (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE feature_viewpoint
  OWNER TO vagrant;


