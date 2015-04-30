package de.i3mainz.ibr.xml;

import de.i3mainz.ibr.database.DBInterface;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class Database extends DBInterface {
	
	public Database() throws SQLException, ClassNotFoundException {
		super();
	}
	
	public ResultSet getSpatialcontexts() throws SQLException {
		String sql = "SELECT spatialcontext.*, transformation.* FROM spatialcontext INNER JOIN transformation ON spatialcontext.IDREF_trans = transformation.id";
		PreparedStatement statement = connection.prepareStatement(sql);
		return statement.executeQuery();
	}
	
	public ResultSet getSpatialcontext(String spatialcontext) throws SQLException {
		String sql = "SELECT spatialcontext.*, transformation.* FROM spatialcontext INNER JOIN transformation ON spatialcontext.IDREF_trans = transformation.id WHERE spatialcontext.name = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		return statement.executeQuery();
	}
	
	public ResultSet getFeatures(String spatialcontext) throws SQLException {
		String sql = "SELECT st_asewkt(feature.geom), feature.id, edit.creator, edit.date FROM feature, spatialcontext, edit WHERE edit.IDREF_feature = feature.id AND feature.IDREF_sc = spatialcontext.id AND spatialcontext.name = ? AND edit.date = (SELECT max(edit.date) FROM edit WHERE edit.IDREF_feature = feature.id)";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		return statement.executeQuery();
	}
	
	public ResultSet getFeature(String spatialcontext, int fid) throws SQLException {
		String sql = "SELECT st_asewkt(feature.geom), feature.id, edit.creator, edit.date FROM feature, spatialcontext, edit WHERE edit.IDREF_feature = feature.id AND feature.IDREF_sc = spatialcontext.id AND spatialcontext.name = ? AND edit.date = (SELECT max(edit.date) FROM edit WHERE edit.IDREF_feature = feature.id AND feature.id = ?)";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		statement.setInt(2,fid);
		return statement.executeQuery();
	}
	
	public ResultSet getFeatureViewpoints(int fid) throws SQLException {
		String sql = "SELECT viewpoint.* FROM viewpoint, feature_viewpoint WHERE feature_viewpoint.IDREF_feature = ? AND feature_viewpoint.IDREF_view = viewpoint.id";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1,fid);
		return statement.executeQuery();
	}
	
	public ResultSet getFloorplans(String spatialcontext) throws SQLException {
		String sql = "SELECT media.*, transformation.* FROM media INNER JOIN transformation ON media.IDREF_trans = transformation.id, spatialcontext WHERE media.IDREF_sc = spatialcontext.id AND spatialcontext.name = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		return statement.executeQuery();
	}
	
	public ResultSet getFloorplanViewpoints(int mid) throws SQLException {
		String sql = "SELECT viewpoint.* FROM viewpoint, media WHERE media.id = ? AND media.IDREF_sc = viewpoint.IDREF_sc";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1,mid);
		return statement.executeQuery();
	}
	
	public ResultSet getViewpoints(String spatialcontext) throws SQLException {
		String sql = "SELECT viewpoint.*, transformation.* FROM viewpoint INNER JOIN transformation ON viewpoint.IDREF_trans = transformation.id, spatialcontext WHERE viewpoint.IDREF_sc = spatialcontext.id AND spatialcontext.name = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setString(1,spatialcontext);
		return statement.executeQuery();
	}
	
	public ResultSet getViewpointFeatures(int vid) throws SQLException {
		String sql = "SELECT feature.* FROM feature, feature_viewpoint WHERE feature_viewpoint.IDREF_view = ? AND feature_viewpoint.IDREF_feature = feature.id";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1,vid);
		return statement.executeQuery();
	}
	
	public ResultSet getViewpointFloorplans(int vid) throws SQLException {
		String sql = "SELECT media.* FROM media, viewpoint WHERE viewpoint.id = ? AND viewpoint.IDREF_sc = media.IDREF_sc";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1,vid);
		return statement.executeQuery();
	}
	
	public ResultSet getViewpointPanoramas(int vid) throws SQLException {
		String sql = "SELECT panorama.*, transformation.* FROM panorama INNER JOIN transformation ON panorama.IDREF_trans = transformation.id WHERE panorama.IDREF_view = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1,vid);
		return statement.executeQuery();
	}
	
	public ResultSet getPanoramaImages(int pid) throws SQLException {
		String sql = "SELECT panoramaimg.* FROM panoramaimg WHERE panoramaimg.IDREF_pano = ?";
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setInt(1,pid);
		return statement.executeQuery();
	}
	
}
