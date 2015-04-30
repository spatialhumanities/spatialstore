package de.i3mainz.ibr.project;

import de.i3mainz.ibr.database.DBInterface;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author s3b31293
 */
public class Database extends DBInterface {

	private boolean SpatialContextExists = false;
	protected String StringOut = "";
	private boolean MediaExists;
	private boolean ViewpointExists;
	private boolean PointcloudExists;
	private boolean PanoramaExists;
	private boolean PanoramaImgExists;

	public Database() throws ClassNotFoundException, SQLException {
		super();
	}
	
    /**
     * Input Spatialcontext and Transformation in Database
     *
     * @param _sc_name
     * @param _sc_place
     * @param _sc_date
     * @param _sc_transsrc
     * @param _sc_transdst
     * @param _trans_param
	 * @param _sc_srid
     * @return SerialID
     * @throws SQLException
     */
	protected int inputSpatialcontextInDB(String _sc_name, String _sc_place, String _sc_date,
			String _sc_transsrc, String _sc_transdst, String _trans_param, String _sc_srid) throws SQLException {

		String ret = "-1";
		SpatialContextExists = false;

		if (_sc_name.equals("") || _sc_name.equals("") && _sc_place.equals("") && _sc_date.equals("") && _sc_transsrc.equals("") && _sc_transdst.equals("") && _trans_param.equals("") && _sc_srid.equals("")) {

			ret = "-1";

		} else {

			////////////////////////////
			// Spatialcontext exists? //
			////////////////////////////
			ResultSet SCexistsend_ResultSet = null;

			try {
				SCexistsend_ResultSet = spatialcontextExistendResultSet(_sc_name);
			} catch (Exception e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputSpatialcontextInDB(scexists)";
				throw new SQLException(StringOut);
			}

			//Anzahl der Spalten
			int lauf = 0;
			int rows = 0;
			try {
				SCexistsend_ResultSet.last();
				rows = SCexistsend_ResultSet.getRow();
				SCexistsend_ResultSet.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputSpatialcontextInDB(scexists)";
				throw new SQLException(StringOut);
			}

			String SCexists = "";

			while (SCexistsend_ResultSet.next() && lauf < rows) {
				SCexists = SCexists + SCexistsend_ResultSet.getString(1);
				lauf++;
			}

			if (SCexists.equals("t")) {
				SpatialContextExists = true;
			} else {
				SpatialContextExists = false;
			}

			//Ausgabe
			if (SCexists.equals("null")) {
				StringOut = StringOut + "<error>an bad error has happend (inputSpatialcontextInDB-spatialcontext exists query)</error>";
			} else {
				StringOut = StringOut + "<input>spatialcontext exists query ok</input>";
			}

			if (SpatialContextExists == false) {

				/////////////////////////////////////
				// Input into Transformation Table //
				/////////////////////////////////////
				ResultSet AKTTRANSID_ResultSet = null;

				try {
					AKTTRANSID_ResultSet = this.inputTransformationGetSerialIDResultSet(_sc_transsrc, _sc_transdst, _trans_param);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputSpatialcontextInDB(transformation)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTTRANSID_ResultSet.last();
					rows = AKTTRANSID_ResultSet.getRow();
					AKTTRANSID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputSpatialcontextInDB(transformation)";
					throw new SQLException(StringOut);
				}

				String AktTransID = "";

				while (AKTTRANSID_ResultSet.next() && lauf < rows) {
					AktTransID = AktTransID + AKTTRANSID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktTransID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputSpatialcontextInDB-transformation)</error>";
				} else {
					StringOut = StringOut + "<input>spatialcontext input (transformation) ok</input>";
				}

				/////////////////////////////////////
				// Input into Spatialcontext Table //
				/////////////////////////////////////
				ResultSet AKTSCID_ResultSet = null;

				try {
					AKTSCID_ResultSet = this.inputSpatialcontextGetSerialIDResultSet(AktTransID, _sc_name, _sc_place, _sc_date, _sc_srid);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputSpatialcontextInDB(spatialcontext)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTSCID_ResultSet.last();
					rows = AKTSCID_ResultSet.getRow();
					AKTSCID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputSpatialcontextInDB(spatialcontext)";
					throw new SQLException(StringOut);
				}

				String AktSCID = "";

				while (AKTSCID_ResultSet.next() && lauf < rows) {
					AktSCID = AktSCID + AKTSCID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktSCID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputSpatialcontextInDB-spatialcontext)</error>";
				} else {
					StringOut = StringOut + "<input>spatialcontext input (spatialcontext) ok</input>";
				}

				ret = AktSCID;

			} else { // SpatialContextExists == true

				//////////////////////////
				// Get SpatialcontextID //
				//////////////////////////
				ResultSet SCID_ResultSet = null;

				try {
					SCID_ResultSet = this.getSpatialcontextSerialIDResultSet(_sc_name);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputSpatialcontextInDB(scexists)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					SCID_ResultSet.last();
					rows = SCID_ResultSet.getRow();
					SCID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputSpatialcontextInDB(scexists)";
					throw new SQLException(StringOut);
				}

				String SC_id = "";

				while (SCID_ResultSet.next() && lauf < rows) {
					SC_id = SC_id + SCID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (SC_id.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputSpatialcontextInDB-spatialcontext serialID query)</error>";
				} else {
					StringOut = StringOut + "<input>spatialcontext serialID query ok</input>";
				}

				ret = SC_id;

			}
		}

		////////////////////////
		// Return aktuelle ID //
		////////////////////////
		return Integer.parseInt(ret);

	}

	/**
	 * Input Media and Transformation in Database
	 *
	 * @param _SC_ID
	 * @param _m_type
	 * @param _m_filename
	 * @param _m_desc
	 * @param _sc_transsrc
	 * @param _sc_transdst
	 * @param _trans_param
	 * @return SerialID
	 * @throws SQLException
	 */
	protected int inputMediaInDB(int _SC_ID, String _m_type, String _m_filename, String _m_desc,
			String _sc_transsrc, String _sc_transdst, String _trans_param) throws SQLException {

		String ret = "-1";
		MediaExists = false;

		if (_SC_ID == -1 || _SC_ID == -1 && _m_type.equals("") && _m_filename.equals("") && _sc_transsrc.equals("") && _sc_transdst.equals("") && _trans_param.equals("") && _m_desc.equalsIgnoreCase("")) {

			ret = "-1";

		} else {

            ////////////////////
			// Media exists? //
			///////////////////
			ResultSet MEDexistsend_ResultSet = null;

			try {
				MEDexistsend_ResultSet = this.mediaExistendResultSet(_m_filename, String.valueOf(_SC_ID));
			} catch (Exception e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputMediaInDB(media-exists)";
				throw new SQLException(StringOut);
			}

			//Anzahl der Spalten
			int lauf = 0;
			int rows = 0;
			try {
				MEDexistsend_ResultSet.last();
				rows = MEDexistsend_ResultSet.getRow();
				MEDexistsend_ResultSet.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputMediaInDB(media-exists)";
				throw new SQLException(StringOut);
			}

			String MEDexists = "";

			while (MEDexistsend_ResultSet.next() && lauf < rows) {
				MEDexists = MEDexists + MEDexistsend_ResultSet.getString(1);
				lauf++;
			}

			if (MEDexists.equals("t")) {
				MediaExists = true;
			} else {
				MediaExists = false;
			}

			//Ausgabe
			if (MEDexists.equals("null")) {
				StringOut = StringOut + "<error>an bad error has happend (inputMediaInDB-media exists query)</error>";
			} else {
				StringOut = StringOut + "<input>media exists query ok</input>";
			}

			if (MediaExists == false) {

                /////////////////////////////////////
				// Input into Transformation Table //
				/////////////////////////////////////
				ResultSet AKTTRANSID_ResultSet = null;

				try {
					AKTTRANSID_ResultSet = this.inputTransformationGetSerialIDResultSet(_sc_transsrc, _sc_transdst, _trans_param);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputMediaInDB(transformation)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTTRANSID_ResultSet.last();
					rows = AKTTRANSID_ResultSet.getRow();
					AKTTRANSID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputMediaInDB(transformation)";
					throw new SQLException(StringOut);
				}

				String AktTransID = "";

				while (AKTTRANSID_ResultSet.next() && lauf < rows) {
					AktTransID = AktTransID + AKTTRANSID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktTransID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputMediaInDB-transformation)</error>";
				} else {
					StringOut = StringOut + "<input>media input (transformation) ok</input>";
				}

                /////////////////////////////
				// Input into Media Table //
				////////////////////////////
				ResultSet AKTMEDID_ResultSet = null;

				try {
					AKTMEDID_ResultSet = this.inputMediaGetSerialIDResultSet(AktTransID, String.valueOf(_SC_ID), _m_type, _m_filename, _m_desc);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputMediaInDB(media)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTMEDID_ResultSet.last();
					rows = AKTMEDID_ResultSet.getRow();
					AKTMEDID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputMediaInDB(media)";
					throw new SQLException(StringOut);
				}

				String AktMEDID = "";

				while (AKTMEDID_ResultSet.next() && lauf < rows) {
					AktMEDID = AktMEDID + AKTMEDID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktMEDID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputMediaInDB-media)</error>";
				} else {
					StringOut = StringOut + "<input>media input (media) ok</input>";
				}

				ret = AktMEDID;

			} else { // MediaExists == true

                /////////////////
				// Get MediaID //
				/////////////////
				ResultSet MEDID_ResultSet = null;

				try {
					MEDID_ResultSet = this.getMediaSerialIDResultSet(_m_filename, String.valueOf(_SC_ID));
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputMediaInDB(medexists)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					MEDID_ResultSet.last();
					rows = MEDID_ResultSet.getRow();
					MEDID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputMediaInDB(medexists)";
					throw new SQLException(StringOut);
				}

				String MED_id = "";

				while (MEDID_ResultSet.next() && lauf < rows) {
					MED_id = MED_id + MEDID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (MED_id.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputMediaInDB-media serialID query)</error>";
				} else {
					StringOut = StringOut + "<input>media serialID query ok</input>";
				}

				ret = MED_id;

			}
		}

        ////////////////////////
		// Return aktuelle ID //
		////////////////////////
		return Integer.parseInt(ret);

	}

	/**
	 * Input Viewpoint and Transformation in Database
	 *
	 * @param _SC_ID
	 * @param _v_name
	 * @param _v_place
	 * @param _v_transsrc
	 * @param _v_transdst
	 * @param _trans_param
	 * @return SerialID
	 * @throws SQLException
	 */
	protected int inputViewpointInDB(int _SC_ID, String _v_name, String _v_place,
			String _v_transsrc, String _v_transdst, String _trans_param) throws SQLException {

		String ret = "-1";
		ViewpointExists = false;

		if (_SC_ID == -1 || _SC_ID == -1 && _v_name.equals("") && _v_place.equals("") && _v_transsrc.equals("") && _v_transdst.equals("") && _trans_param.equals("")) {

			ret = "-1";

		} else {

            ///////////////////////
			// Viewpoint exists? //
			///////////////////////
			ResultSet VPexistsend_ResultSet = null;

			try {
				VPexistsend_ResultSet = this.viewpointExistendResultSet(_v_name, String.valueOf(_SC_ID));
			} catch (Exception e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputViewpointInDB(viewpoint-exists)";
				throw new SQLException(StringOut);
			}

			//Anzahl der Spalten
			int lauf = 0;
			int rows = 0;
			try {
				VPexistsend_ResultSet.last();
				rows = VPexistsend_ResultSet.getRow();
				VPexistsend_ResultSet.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputMediaInDB(media-exists)";
				throw new SQLException(StringOut);
			}

			String VPexists = "";

			while (VPexistsend_ResultSet.next() && lauf < rows) {
				VPexists = VPexists + VPexistsend_ResultSet.getString(1);
				lauf++;
			}

			if (VPexists.equals("t")) {
				ViewpointExists = true;
			} else {
				ViewpointExists = false;
			}

			//Ausgabe
			if (VPexists.equals("null")) {
				StringOut = StringOut + "<error>an bad error has happend (inputViewpointInDB-viewpoint exists query)</error>";
			} else {
				StringOut = StringOut + "<input>viewpoint exists query ok</input>";
			}

			if (ViewpointExists == false) {

                /////////////////////////////////////
				// Input into Transformation Table //
				/////////////////////////////////////
				ResultSet AKTTRANSID_ResultSet = null;

				try {
					AKTTRANSID_ResultSet = this.inputTransformationGetSerialIDResultSet(_v_transsrc, _v_transdst, _trans_param);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputViewpointInDB(transformation)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTTRANSID_ResultSet.last();
					rows = AKTTRANSID_ResultSet.getRow();
					AKTTRANSID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputViewpointInDB(transformation)";
					throw new SQLException(StringOut);
				}

				String AktTransID = "";

				while (AKTTRANSID_ResultSet.next() && lauf < rows) {
					AktTransID = AktTransID + AKTTRANSID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktTransID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputViewpointInDB-transformation)</error>";
				} else {
					StringOut = StringOut + "<input>viewpoint input (transformation) ok</input>";
				}

                ////////////////////////////////
				// Input into Viewpoint Table //
				////////////////////////////////
				ResultSet AKTVPID_ResultSet = null;

				try {
					AKTVPID_ResultSet = this.inputViewpointGetSerialIDResultSet(AktTransID, String.valueOf(_SC_ID), _v_name, _v_place);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputViewpointInDB(viewpoint)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTVPID_ResultSet.last();
					rows = AKTVPID_ResultSet.getRow();
					AKTVPID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputViewpointInDB(viewpoint)";
					throw new SQLException(StringOut);
				}

				String AktVPCID = "";

				while (AKTVPID_ResultSet.next() && lauf < rows) {
					AktVPCID = AktVPCID + AKTVPID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktVPCID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputViewpointInDB-viewpoint)</error>";
				} else {
					StringOut = StringOut + "<input>viewpoint input (viewpoint) ok</input>";
				}

				ret = AktVPCID;

			} else { // ViewpointExists == true

                /////////////////////
				// Get ViewpointID //
				/////////////////////
				ResultSet VPID_ResultSet = null;

				try {
					VPID_ResultSet = this.getViewpointSerialIDResultSet(_v_name, String.valueOf(_SC_ID));
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputViewpointInDB(vpexists)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					VPID_ResultSet.last();
					rows = VPID_ResultSet.getRow();
					VPID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputViewpointInDB(vpexists)";
					throw new SQLException(StringOut);
				}

				String VP_id = "";

				while (VPID_ResultSet.next() && lauf < rows) {
					VP_id = VP_id + VPID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (VP_id.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputViewpointInDB-viewpoint serialID query)</error>";
				} else {
					StringOut = StringOut + "<input>viewpoint serialID query ok</input>";
				}

				ret = VP_id;

			}
		}

        ////////////////////////
		// Return aktuelle ID //
		////////////////////////
		return Integer.parseInt(ret);

	}

	/**
	 * Input Pointcloud and Transformation in Database
	 *
	 * @param _VP_ID
	 * @param _p_filename
	 * @param _p_type
	 * @param _p_bboxlocal
	 * @param _p_bboxglobal
	 * @param _p_bboxpolar
	 * @param _p_remrange
	 * @param _p_rows
	 * @param _p_cols
	 * @param _p_transsrc
	 * @param _p_transdst
	 * @param _trans_param
	 * @return SerialID
	 * @throws SQLException
	 */
	protected int inputPointcloudInDB(int _VP_ID, String _p_filename, String _p_type,
			String _p_bboxlocal, String _p_bboxglobal, String _p_bboxpolar, String _p_remrange, String _p_rows, String _p_cols,
			String _p_transsrc, String _p_transdst, String _trans_param) throws SQLException {

		String ret = "-1";
		PointcloudExists = false;

		if (_VP_ID == -1 || _VP_ID == -1 && _p_filename.equals("") && _p_type.equals("") && _p_bboxlocal.equals("")
				&& _p_bboxglobal.equals("") && _p_bboxpolar.equals("") && _p_remrange.equals("") && _p_rows.equals("")
				&& _p_cols.equals("") && _p_transsrc.equals("") && _p_transdst.equals("") && _trans_param.equals("")) {

			ret = "-1";

		} else {

            ////////////////////////
			// Pointcloud exists? //
			////////////////////////
			ResultSet PCexistsend_ResultSet = null;

			try {
				PCexistsend_ResultSet = this.pointcloudExistendResultSet(_p_filename, String.valueOf(_VP_ID));
			} catch (Exception e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputPointcloudInDB(pointcloud-exists)";
				throw new SQLException(StringOut);
			}

			//Anzahl der Spalten
			int lauf = 0;
			int rows = 0;
			try {
				PCexistsend_ResultSet.last();
				rows = PCexistsend_ResultSet.getRow();
				PCexistsend_ResultSet.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputPointcloudInDB(pointcloud-exists)";
				throw new SQLException(StringOut);
			}

			String PCexists = "";

			while (PCexistsend_ResultSet.next() && lauf < rows) {
				PCexists = PCexists + PCexistsend_ResultSet.getString(1);
				lauf++;
			}

			if (PCexists.equals("t")) {
				PointcloudExists = true;
			} else {
				PointcloudExists = false;
			}

			//Ausgabe
			if (PCexists.equals("null")) {
				StringOut = StringOut + "<error>an bad error has happend (inputPointcloudInDB-pointcloud exists query)</error>";
			} else {
				StringOut = StringOut + "<input>pointcloud exists query ok</input>";
			}

			if (PointcloudExists == false) {

                /////////////////////////////////////
				// Input into Transformation Table //
				/////////////////////////////////////
				ResultSet AKTTRANSID_ResultSet = null;

				try {
					AKTTRANSID_ResultSet = this.inputTransformationGetSerialIDResultSet(_p_transsrc, _p_transdst, _trans_param);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPointcloudInDB(transformation)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTTRANSID_ResultSet.last();
					rows = AKTTRANSID_ResultSet.getRow();
					AKTTRANSID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPointcloudInDB(transformation)";
					throw new SQLException(StringOut);
				}

				String AktTransID = "";

				while (AKTTRANSID_ResultSet.next() && lauf < rows) {
					AktTransID = AktTransID + AKTTRANSID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktTransID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPointcloudInDB-transformation)</error>";
				} else {
					StringOut = StringOut + "<input>pointcloud input (transformation) ok</input>";
				}

                /////////////////////////////////
				// Input into Pointcloud Table //
				/////////////////////////////////
				ResultSet AKTPCID_ResultSet = null;

				try {
					AKTPCID_ResultSet = this.inputPointcloudGetSerialIDResultSet(AktTransID, String.valueOf(_VP_ID), _p_filename, _p_type,
							_p_bboxlocal, _p_bboxglobal, _p_bboxpolar, _p_remrange, _p_rows, _p_cols);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPointcloudInDB(pointcloud)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTPCID_ResultSet.last();
					rows = AKTPCID_ResultSet.getRow();
					AKTPCID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPointcloudInDB(pointcloud)";
					throw new SQLException(StringOut);
				}

				String AktPCID = "";

				while (AKTPCID_ResultSet.next() && lauf < rows) {
					AktPCID = AktPCID + AKTPCID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktPCID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPointcloudInDB-pointcloud)</error>";
				} else {
					StringOut = StringOut + "<input>pointcloud input (pointcloud) ok</input>";
				}

				ret = AktPCID;

			} else { // PointcloudExists == true

                //////////////////////
				// Get PointcloudID //
				//////////////////////
				ResultSet PCID_ResultSet = null;

				try {
					PCID_ResultSet = this.getPointcloudSerialIDResultSet(_p_filename, String.valueOf(_VP_ID));
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPointcloudInDB(pcexists)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					PCID_ResultSet.last();
					rows = PCID_ResultSet.getRow();
					PCID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPointcloudInDB(pcexists)";
					throw new SQLException(StringOut);
				}

				String PC_id = "";

				while (PCID_ResultSet.next() && lauf < rows) {
					PC_id = PC_id + PCID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (PC_id.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPointcloudInDB-pointcloud serialID query)</error>";
				} else {
					StringOut = StringOut + "<input>pointcloud serialID query ok</input>";
				}

				ret = PC_id;

			}
		}

        ////////////////////////
		// Return aktuelle ID //
		////////////////////////
		return Integer.parseInt(ret);

	}

	/**
	 * Input Panorama and Transformation in Database
	 *
	 * @param _VP_ID
	 * @param _p_structtype
	 * @param _p_kindof
	 * @param _p_transsrc
	 * @param _p_transdst
	 * @param _trans_param
	 * @return SerialID
	 * @throws SQLException
	 */
	protected int inputPanoramaInDB(int _VP_ID, String _p_structtype, String _p_kindof,
			String _p_transsrc, String _p_transdst, String _trans_param) throws SQLException {

		String ret = "-1";
		PanoramaExists = false;

		if (_VP_ID == -1 || _VP_ID == -1 && _p_structtype.equals("") && _p_kindof.equals("")
				&& _p_transsrc.equals("") && _p_transdst.equals("") && _trans_param.equals("")) {

			ret = "-1";

		} else {

            ////////////////////////
			// Panorama exists? //
			////////////////////////
			ResultSet PANexistsend_ResultSet = null;

			try {
				PANexistsend_ResultSet = this.panoramaExistendResultSet(_p_structtype, _p_kindof, String.valueOf(_VP_ID));
			} catch (Exception e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputPanoramaInDB(panorama-exists)";
				throw new SQLException(StringOut);
			}

			//Anzahl der Spalten
			int lauf = 0;
			int rows = 0;
			try {
				PANexistsend_ResultSet.last();
				rows = PANexistsend_ResultSet.getRow();
				PANexistsend_ResultSet.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputPanoramaInDB(panorama-exists)";
				throw new SQLException(StringOut);
			}

			String PANexists = "";

			while (PANexistsend_ResultSet.next() && lauf < rows) {
				PANexists = PANexists + PANexistsend_ResultSet.getString(1);
				lauf++;
			}

			if (PANexists.equals("t")) {
				PanoramaExists = true;
			} else {
				PanoramaExists = false;
			}

			//Ausgabe
			if (PANexists.equals("null")) {
				StringOut = StringOut + "<error>an bad error has happend (inputPanoramaInDB-panorama exists query)</error>";
			} else {
				StringOut = StringOut + "<input>panorama exists query ok</input>";
			}

			if (PanoramaExists == false) {

                /////////////////////////////////////
				// Input into Transformation Table //
				/////////////////////////////////////
				ResultSet AKTTRANSID_ResultSet = null;

				try {
					AKTTRANSID_ResultSet = this.inputTransformationGetSerialIDResultSet(_p_transsrc, _p_transdst, _trans_param);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaInDB(transformation)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTTRANSID_ResultSet.last();
					rows = AKTTRANSID_ResultSet.getRow();
					AKTTRANSID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaInDB(transformation)";
					throw new SQLException(StringOut);
				}

				String AktTransID = "";

				while (AKTTRANSID_ResultSet.next() && lauf < rows) {
					AktTransID = AktTransID + AKTTRANSID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktTransID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPanoramaInDB-transformation)</error>";
				} else {
					StringOut = StringOut + "<input>panorama input (transformation) ok</input>";
				}

                ///////////////////////////////
				// Input into Panorama Table //
				///////////////////////////////
				ResultSet AKTPANID_ResultSet = null;

				try {
					AKTPANID_ResultSet = this.inputPanoramaGetSerialIDResultSet(AktTransID, String.valueOf(_VP_ID), _p_structtype, _p_kindof);
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaInDB(panorama)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTPANID_ResultSet.last();
					rows = AKTPANID_ResultSet.getRow();
					AKTPANID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaInDB(panorama)";
					throw new SQLException(StringOut);
				}

				String AktPANID = "";

				while (AKTPANID_ResultSet.next() && lauf < rows) {
					AktPANID = AktPANID + AKTPANID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktPANID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPanoramaInDB-panorama)</error>";
				} else {
					StringOut = StringOut + "<input>panorama input (panorama) ok</input>";
				}

				ret = AktPANID;

			} else { // PanoramaExists == true

                ////////////////////
				// Get PanoramaID //
				////////////////////
				ResultSet PANID_ResultSet = null;

				try {
					PANID_ResultSet = this.getPanoramaSerialIDResultSet(_p_structtype, _p_kindof, String.valueOf(_VP_ID));
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaInDB(panexists)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					PANID_ResultSet.last();
					rows = PANID_ResultSet.getRow();
					PANID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaInDB(panexists)";
					throw new SQLException(StringOut);
				}

				String PAN_id = "";

				while (PANID_ResultSet.next() && lauf < rows) {
					PAN_id = PAN_id + PANID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (PAN_id.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPanoramaInDB-panorama serialID query)</error>";
				} else {
					StringOut = StringOut + "<input>panorama serialID query ok</input>";
				}

				ret = PAN_id;

			}
		}

        ////////////////////////
		// Return aktuelle ID //
		////////////////////////
		return Integer.parseInt(ret);

	}

	/**
	 * Input PanoramaImage in Database
	 *
	 * @param _PAN_ID
	 * @param _pi_imgpath
	 * @param _pi_order
	 * @return SerialID
	 * @throws SQLException
	 */
	protected int inputPanoramaImgInDB(int _PAN_ID, String _pi_imgpath, int _pi_order) throws SQLException {

		String ret = "-1";
		PanoramaImgExists = false;

		if (_PAN_ID == -1 || _PAN_ID == -1 && _pi_imgpath.equals("")) {

			ret = "-1";

		} else {

            ////////////////////////////
			// PanoramaImage exists? //
			////////////////////////////
			ResultSet PANIMGexistsend_ResultSet = null;

			try {
				PANIMGexistsend_ResultSet = this.panoramaImgExistendResultSet(_pi_imgpath, String.valueOf(_PAN_ID));
			} catch (Exception e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputPanoramaImgInDB(panoramaImg-exists)";
				throw new SQLException(StringOut);
			}

			//Anzahl der Spalten
			int lauf = 0;
			int rows = 0;
			try {
				PANIMGexistsend_ResultSet.last();
				rows = PANIMGexistsend_ResultSet.getRow();
				PANIMGexistsend_ResultSet.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
				StringOut = e.toString() + "from inputPanoramaImgInDB(panoramaImg-exists)";
				throw new SQLException(StringOut);
			}

			String PANIMGexists = "";

			while (PANIMGexistsend_ResultSet.next() && lauf < rows) {
				PANIMGexists = PANIMGexists + PANIMGexistsend_ResultSet.getString(1);
				lauf++;
			}

			if (PANIMGexists.equals("t")) {
				PanoramaImgExists = true;
			} else {
				PanoramaImgExists = false;
			}

			//Ausgabe
			if (PANIMGexists.equals("null")) {
				StringOut = StringOut + "<error>an bad error has happend (inputPanoramaImgInDB-panoramaImg exists query)</error>";
			} else {
				StringOut = StringOut + "<input>panoramaImg exists query ok</input>";
			}

			if (PanoramaImgExists == false) {

                //////////////////////////////////
				// Input into PanoramaImg Table //
				//////////////////////////////////
				ResultSet AKTPANID_ResultSet = null;

				try {
					AKTPANID_ResultSet = this.inputPanoramaImgGetSerialIDResultSet(String.valueOf(_PAN_ID), _pi_imgpath, String.valueOf(_pi_order));
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaImgInDB(panoramaImg)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					AKTPANID_ResultSet.last();
					rows = AKTPANID_ResultSet.getRow();
					AKTPANID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaImgInDB(panoramaImg)";
					throw new SQLException(StringOut);
				}

				String AktPANIMGID = "";

				while (AKTPANID_ResultSet.next() && lauf < rows) {
					AktPANIMGID = AktPANIMGID + AKTPANID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (AktPANIMGID.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPanoramaImgInDB-panoramaImg)</error>";
				} else {
					StringOut = StringOut + "<input>panoramaImg input (panoramaImg) ok</input>";
				}

				ret = AktPANIMGID;

			} else { // PanoramaImgExists == true

                ////////////////////
				// Get PanoramaID //
				////////////////////
				ResultSet PANIMGID_ResultSet = null;

				try {
					PANIMGID_ResultSet = this.getPanoramaImgSerialIDResultSet(_pi_imgpath, String.valueOf(_PAN_ID));
				} catch (Exception e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaImgInDB(panimgexists)";
					throw new SQLException(StringOut);
				}

				//Anzahl der Spalten
				lauf = 0;
				rows = 0;
				try {
					PANIMGID_ResultSet.last();
					rows = PANIMGID_ResultSet.getRow();
					PANIMGID_ResultSet.beforeFirst();
				} catch (SQLException e) {
					e.printStackTrace();
					StringOut = e.toString() + "from inputPanoramaImgInDB(panimgexists)";
					throw new SQLException(StringOut);
				}

				String PANIMG_id = "";

				while (PANIMGID_ResultSet.next() && lauf < rows) {
					PANIMG_id = PANIMG_id + PANIMGID_ResultSet.getString(1);
					lauf++;
				}

				//Ausgabe
				if (PANIMG_id.equals("null")) {
					StringOut = StringOut + "<error>an bad error has happend (inputPanoramaImgInDB-panoramaImg serialID query)</error>";
				} else {
					StringOut = StringOut + "<input>panoramaImg serialID query ok</input>";
				}

				ret = PANIMG_id;

			}
		}

        ////////////////////////
		// Return aktuelle ID //
		////////////////////////
		return Integer.parseInt(ret);

	}

	private ResultSet spatialcontextExistendResultSet(String SC_name) throws SQLException {
		String querySelect = "SELECT existendSpatialcontext('" + SC_name + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet inputTransformationGetSerialIDResultSet(String srccrs, String dstcrs, String params) throws SQLException {
		String querySelect = "SELECT inputTransformation('" + srccrs + "','" + dstcrs + "','" + params + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet inputSpatialcontextGetSerialIDResultSet(String SC_transID, String SC_name, String SC_place, String SC_date, String SC_srid) throws SQLException {
		String querySelect = "SELECT inputSpatialcontext(" + SC_transID + ",'" + SC_name + "','" + SC_place + "','" + SC_date + "','" + SC_srid + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet getSpatialcontextSerialIDResultSet(String SC_name) throws SQLException {
		String querySelect = "SELECT idSpatialcontext('" + SC_name + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet mediaExistendResultSet(String M_filename, String SC_ID) throws SQLException {
		String querySelect = "SELECT existendMedia('" + M_filename + "'," + SC_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet getMediaSerialIDResultSet(String M_filename, String SC_ID) throws SQLException {
		String querySelect = "SELECT idMedia('" + M_filename + "'," + SC_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet inputMediaGetSerialIDResultSet(String M_transID, String SC_ID, String M_type, String M_filename, String M_desc) throws SQLException {
		String querySelect = "SELECT inputMedia(" + M_transID + "," + SC_ID + ",'" + M_type + "','" + M_filename + "','" + M_desc + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet viewpointExistendResultSet(String V_name, String SC_ID) throws SQLException {
		String querySelect = "SELECT existendViewpoint('" + V_name + "'," + SC_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet getViewpointSerialIDResultSet(String V_name, String SC_ID) throws SQLException {
		String querySelect = "SELECT idViewpoint('" + V_name + "'," + SC_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet inputViewpointGetSerialIDResultSet(String V_transID, String SC_ID, String V_name, String V_place) throws SQLException {
		String querySelect = "SELECT inputViewpoint(" + V_transID + "," + SC_ID + ",'" + V_name + "','" + V_place + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet pointcloudExistendResultSet(String P_filename, String VP_ID) throws SQLException {
		String querySelect = "SELECT existendPointcloud('" + P_filename + "'," + VP_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet getPointcloudSerialIDResultSet(String P_filename, String VP_ID) throws SQLException {
		String querySelect = "SELECT idPointcloud('" + P_filename + "'," + VP_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet inputPointcloudGetSerialIDResultSet(String P_transID, String VP_ID, String P_filename, String P_type,
			String P_bboxlocal, String P_bboxglobal, String P_bboxpolar, String P_remrange, String P_rows, String P_cols) throws SQLException {
		String querySelect = "SELECT inputPointcloud(" + P_transID + "," + VP_ID + ",'" + P_filename + "','" + P_type + "','"
				+ P_bboxlocal + "','" + P_bboxglobal + "','" + P_bboxpolar + "','" + P_remrange + "'," + P_rows + "," + P_cols + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet panoramaExistendResultSet(String P_structtype, String P_kindof, String VP_ID) throws SQLException {
		String querySelect = "SELECT existendPanorama('" + P_structtype + "','" + P_kindof + "'," + VP_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet getPanoramaSerialIDResultSet(String P_structtype, String P_kindof, String VP_ID) throws SQLException {
		String querySelect = "SELECT idPanorama('" + P_structtype + "','" + P_kindof + "'," + VP_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet inputPanoramaGetSerialIDResultSet(String P_transID, String VP_ID, String P_structtype, String P_kindof) throws SQLException {
		String querySelect = "SELECT inputPanorama(" + P_transID + "," + VP_ID + ",'" + P_structtype + "','" + P_kindof + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet panoramaImgExistendResultSet(String PI_imgpath, String PAN_ID) throws SQLException {
		String querySelect = "SELECT existendPanoramaImg('" + PI_imgpath + "'," + PAN_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet getPanoramaImgSerialIDResultSet(String PI_imgpath, String PAN_ID) throws SQLException {
		String querySelect = "SELECT idPanoramaImg('" + PI_imgpath + "'," + PAN_ID + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	private ResultSet inputPanoramaImgGetSerialIDResultSet(String PAN_ID, String P_imgpath, String P_order) throws SQLException {
		String querySelect = "SELECT inputPanoramaImg(" + PAN_ID + ",'" + P_imgpath + "'," + P_order + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	/*
	 Lösche Funktionen
	 */
	protected ResultSet clearDatabaseCreateNewResultSet() throws SQLException {
		String querySelect = "SELECT clearDatabase()";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	protected ResultSet clearDatabaseExceptTransformationResultSet(String Spatialcontext) throws SQLException {
		String querySelect = "SELECT clearDatabase('" + Spatialcontext + "')";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	protected ResultSet getTransformationIDsResultSet() throws SQLException {
		String querySelect = "SELECT GetSetOfTransformationIDs()";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}

	protected ResultSet deleteNotRequiredTransformationResultSet(int TransID) throws SQLException {
		String querySelect = "SELECT clearTrans(" + String.valueOf(TransID) + ")";
		ResultSet resultSetSelect = getQueryResult(querySelect);
		return resultSetSelect;
	}


	/**
	 * Methode um eine Query zu übergeben und ein ResultSet zu erhalten
	 *
	 * @param query
	 * @return ResultSet
	 */
	public ResultSet getQueryResult(String query) throws SQLException {
		ResultSet resultSet = null;
		try {
			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			resultSet = statement.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
			StringOut = e.toString() + "from getQueryResult()";
			throw new SQLException(StringOut);
			//System.exit(1);
		}
		return resultSet;
	}
}
