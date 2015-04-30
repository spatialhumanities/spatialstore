package de.i3mainz.ibr.getfeature;

import de.i3mainz.ibr.connections.ClientException;
import de.i3mainz.ibr.connections.Config;
import de.i3mainz.ibr.database.DBInterface;
import de.i3mainz.ibr.geometry.Angle;
import de.i3mainz.ibr.geometry.GeoFeature;
import de.i3mainz.ibr.geometry.Util;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

class Database extends DBInterface {
	
	public Database() throws SQLException, ClassNotFoundException {
		super();
	}
	
	public ResultSet getFeatures(String format, String spatialcontext, String viewpoint, String creator) throws SQLException {
		String vp = (viewpoint != null && !viewpoint.isEmpty()) ? ", viewpoint, feature_viewpoint" : "";
		String sql = "SELECT "+format+", feature.id, edit.creator, edit.date FROM feature, edit, spatialcontext"+vp+" "
                        + "WHERE edit.IDREF_feature = feature.id "
                        + "AND feature.IDREF_sc = spatialcontext.id "
                        + "AND spatialcontext.name = ? "
                        + "AND edit.date = (SELECT max(edit.date) FROM edit WHERE edit.IDREF_feature = feature.id)";
		if (viewpoint != null && !viewpoint.isEmpty()) {
			sql += " AND feature_viewpoint.IDREF_feature = feature.id "
                                + "AND feature_viewpoint.IDREF_view = viewpoint.id "
                                + "AND viewpoint.name = ?";
		}
		if (creator != null && !creator.isEmpty()) {
			sql += " AND edit.creator = ?";
		}
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		if (viewpoint != null && !viewpoint.isEmpty()) {
			statement.setString(2,viewpoint);
		}
		if (creator != null && !creator.isEmpty()) {
			statement.setString(viewpoint != null && !viewpoint.isEmpty() ? 3 : 2,creator);
		}
		return statement.executeQuery();
	}
	
	public GeoFeature getFeature(String spatialcontext, int fid) throws SQLException, ClientException {
		String sql = "SELECT st_asewkt(feature.geom), feature.id, edit.creator, edit.date FROM feature, spatialcontext, edit "
                        + "WHERE edit.IDREF_feature = feature.id "
                        + "AND feature.IDREF_sc = spatialcontext.id "
                        + "AND spatialcontext.name = ? "
                        + "AND edit.date = (SELECT max(edit.date) FROM edit WHERE edit.IDREF_feature = feature.id AND feature.id = ?)";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		statement.setInt(2,fid);
		ResultSet result = statement.executeQuery();
		if (!result.next())
			throw new ClientException("feature "+fid+" does not exist",404);
		GeoFeature feature = Util.wktToFeature(result.getString(1));
		if (result.next())
			Config.warn("more than one feature with id "+fid+" in the database");
		return feature;
	}
	
        /**
         * Returns ResultSet with transformation parameters for the given feature. 
         * Contains viewpoint transformation parameters ('viewpoint_trans') 
         * and pointcloud transformation parameters ('pointcloud_trans').
         * 
         * @param spatialcontext name of spatialcontext
         * @param fid feature id
         * @return
         * @throws SQLException 
         */
	public ResultSet getViewpoints(String spatialcontext, int fid) throws SQLException {
		// old: String sql = "SELECT viewpoint.*, pointcloud.*, vt.param AS viewpoint_trans, pt.param AS pointcloud_trans FROM viewpoint INNER JOIN transformation vt ON viewpoint.IDREF_trans = vt.id, pointcloud INNER JOIN transformation pt ON pointcloud.IDREF_trans = pt.id, feature_viewpoint WHERE feature_viewpoint.IDREF_feature = ? AND feature_viewpoint.IDREF_view = viewpoint.id AND pointcloud.IDREF_view = viewpoint.id";
		String sql = "SELECT viewpoint.*, pointcloud.*, vt.param AS viewpoint_trans, pt.param AS pointcloud_trans, st.param as spatialcontext_trans, st.dstcrs as sc_trans_dstcrs "
                        + "FROM viewpoint inner join spatialcontext sc on viewpoint.IDREF_sc = sc.id "
                        + "inner join transformation st on sc.IDREF_trans = st.id "
                        + "INNER JOIN transformation vt ON viewpoint.IDREF_trans = vt.id, pointcloud "
                        + "INNER JOIN transformation pt ON pointcloud.IDREF_trans = pt.id, feature_viewpoint "
                        + "WHERE sc.name = ? "
                        + "and feature_viewpoint.IDREF_feature = ? "
                        + "AND feature_viewpoint.IDREF_view = viewpoint.id "
                        + "AND pointcloud.IDREF_view = viewpoint.id";

                PreparedStatement statement = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		statement.setString(1,spatialcontext);
                statement.setInt(2,fid);
		return statement.executeQuery();
	}
	
        /**
         * Returns ResultSet with all transformation parameters for the given sc. 
         * Contains viewpoint transformation parameters ('viewpoint_trans') 
         * and pointcloud transformation parameters ('pointcloud_trans').
         * 
         * @param spatialcontext
         * @return
         * @throws SQLException 
         */
	public ResultSet getViewpoints(String spatialcontext) throws SQLException {
		String sql = "SELECT viewpoint.*, pointcloud.*, vt.param AS viewpoint_trans, pt.param AS pointcloud_trans, st.param AS spatialcontext_trans, st.dstcrs AS st_dst "
                        + "FROM viewpoint "
                        + "inner join spatialcontext sc on viewpoint.IDREF_sc = sc.id "
                        + "INNER JOIN transformation st ON sc.IDREF_trans = st.id "
                        + "INNER JOIN transformation vt ON viewpoint.IDREF_trans = vt.id, pointcloud "
                        + "INNER JOIN transformation pt ON pointcloud.IDREF_trans = pt.id, spatialcontext "
                        + "WHERE spatialcontext.name = ? "
                        + "AND spatialcontext.id = viewpoint.IDREF_sc "
                        + "AND pointcloud.IDREF_view = viewpoint.id";
		PreparedStatement statement = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
		statement.setString(1,spatialcontext);
		return statement.executeQuery();
	}
	
        /**
         * Returns ResultSet with transformation parameters for the given sc and viewpoint. 
         * Contains viewpoint transformation parameters ('viewpoint_trans') 
         * and pointcloud transformation parameters ('pointcloud_trans').
         * 
         * @param spatialcontext ID of spatial context
         * @param viewpoint ID of viewpoint
         * @return
         * @throws SQLException 
         */
	public ResultSet getViewpoint(String spatialcontext, String viewpoint) throws SQLException {
		String sql = "SELECT viewpoint.*, pointcloud.*, vt.param AS viewpoint_trans, pt.param AS pointcloud_trans FROM viewpoint INNER JOIN transformation vt ON viewpoint.IDREF_trans = vt.id, pointcloud INNER JOIN transformation pt ON pointcloud.IDREF_trans = pt.id, spatialcontext WHERE spatialcontext.name = ? AND spatialcontext.id = viewpoint.IDREF_sc AND viewpoint.name = ? AND pointcloud.IDREF_view = viewpoint.id";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		statement.setString(2,viewpoint);
		return statement.executeQuery();
	}
	
	public int addFeature(String spatialcontext, String geom, String format, int srid) throws SQLException, Exception {
		// TODO: take format into account
		String sql = "INSERT INTO feature (IDREF_sc,geom) SELECT id,ST_GeomFromText(?,?) FROM spatialcontext WHERE name = ?";
		PreparedStatement statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setString(1,geom);
		statement.setInt(2,srid);
		statement.setString(3,spatialcontext);
		statement.executeUpdate();
		ResultSet result = statement.getGeneratedKeys();
		if (!result.next())
			throw new Exception("no key generated during addFeature");
		return result.getInt(1);
	}
	
	public void setVisibility(String spatialcontext, int fid, String viewpoint, boolean visible) throws SQLException {
		String test = "SELECT feature_viewpoint.* FROM feature_viewpoint, viewpoint, spatialcontext WHERE feature_viewpoint.IDREF_feature = ? AND feature_viewpoint.IDREF_view = viewpoint.id AND viewpoint.name = ? AND viewpoint.IDREF_sc = spatialcontext.id AND spatialcontext.name = ?";
		PreparedStatement stm = connection.prepareStatement(test);
		stm.setInt(1,fid);
		stm.setString(2,viewpoint);
		stm.setString(3,spatialcontext);
		ResultSet result = stm.executeQuery();
		if (result.next() != visible) {
			String sql = visible ? "INSERT INTO feature_viewpoint (IDREF_feature, IDREF_view) SELECT ?,viewpoint.id FROM viewpoint, spatialcontext WHERE viewpoint.name = ? AND viewpoint.IDREF_sc = spatialcontext.id AND spatialcontext.name = ?" : 
					"DELETE FROM feature_viewpoint WHERE IDREF_feature = ? AND IDREF_view IN (SELECT viewpoint.id FROM viewpoint, spatialcontext WHERE viewpoint.name = ? AND viewpoint.IDREF_sc = spatialcontext.id AND spatialcontext.name = ?)";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1,fid);
			statement.setString(2,viewpoint);
			statement.setString(3,spatialcontext);
			statement.executeUpdate();
		}
	}
	
	public void setFeature(int fid, String spatialcontext, String geom, String format, int srid) throws SQLException, Exception {
		// TODO: take format into account
		String sql = "UPDATE feature SET geom = ST_GeomFromText(?,?) WHERE id = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,geom);
		statement.setInt(2,srid);
		statement.setInt(3,fid);
		if (statement.executeUpdate() <= 0)
			throw new Exception("feature " + fid + " could not be updated");
	}
	
	public void removeFeature(int fid, String spatialcontext) throws SQLException, Exception {
		String sql = "DELETE FROM feature WHERE id = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1,fid);
		if (statement.executeUpdate() <= 0)
			throw new Exception("feature " + fid + " could not be deleted");
	}
	
	public int addMeasurement(int fid, String spatialcontext, String viewpoint, InputXML input) throws SQLException, Exception {
		Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
		int eid = addEdit(fid,input.getCreator(),timestamp);
		int id = addMeasurement(eid,input);
		Angle[] angles = input.getAngles();
		for (int i=0; i<angles.length; i++) {
			addMeasurementPoint(id,i,angles[i]);
		}
		return id;
	}
	
	public int addImport(int fid, String creator, String license) throws SQLException, Exception {
		Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
		int eid = addEdit(fid,creator,timestamp);
		return addImport(eid,license);
	}
	
	private int addEdit(int fid, String creator, Timestamp date) throws SQLException, Exception {
		String sql = "INSERT INTO edit (IDREF_feature, creator, date) VALUES (?,?,?)";
		PreparedStatement statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setInt(1,fid);
		statement.setString(2,creator);
		statement.setTimestamp(3,date);
		statement.executeUpdate();
		ResultSet result = statement.getGeneratedKeys();
		if (!result.next())
			throw new Exception("no key generated during addEdit");
		return result.getInt(1);
	}
	
	private int addMeasurementPoint(int mid, int order, Angle angle) throws SQLException, Exception {
		String sql = "INSERT INTO imgmeasurement_point (IDREF_imgmeas, phi, theta, pointorder) VALUES (?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setInt(1,mid);
		statement.setDouble(2,angle.getAzim());
		statement.setDouble(3,angle.getElev());
		statement.setInt(4,order);
		statement.executeUpdate();
		ResultSet result = statement.getGeneratedKeys();
		if (!result.next())
			throw new Exception("no key generated during addMeasurementPoint");
		return result.getInt(1);
	}
	
	private int addMeasurement(int eid, InputXML input) throws SQLException, Exception {
		String sql = "INSERT INTO imgmeasurement (IDREF_pano, IDREF_edit, zoom, resolution, geomtype) VALUES (?,?,?,?,?)";
		PreparedStatement statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setInt(1,input.getPanorama());
		statement.setInt(2,eid);
		statement.setDouble(3,input.getZoom());
		statement.setDouble(4,input.getHeight()*input.getWidth());
		statement.setString(5,input.getType());
		statement.executeUpdate();
		ResultSet result = statement.getGeneratedKeys();
		if (!result.next())
			throw new Exception("no key generated during addMeasurement");
		return result.getInt(1);
	}
	
	private int addImport(int eid, String license) throws SQLException, Exception {
		String sql = "INSERT INTO import (IDREF_edit, license) VALUES (?,?)";
		PreparedStatement statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
		statement.setInt(1,eid);
		statement.setString(2,license);
		statement.executeUpdate();
		ResultSet result = statement.getGeneratedKeys();
		if (!result.next())
			throw new Exception("no key generated during addImport");
		return result.getInt(1);
	}
        
        public ResultSet query(String query) throws SQLException, Exception {
            PreparedStatement statement = connection.prepareStatement(query);
            System.out.println(statement);
            statement.executeQuery();
            return statement.getResultSet();
        }
	
}
