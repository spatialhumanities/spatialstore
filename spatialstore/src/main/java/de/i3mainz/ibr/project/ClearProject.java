package de.i3mainz.ibr.project;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author s3b31293
 */
public class ClearProject {
    /**
     * Clear Database, everything
     *
	 * @return 
     * @throws SQLException
     */
	public static String All() throws SQLException {
		String stringOut="";

			ResultSet clearDatabase_ResultSet = null;

			try(Database db = new Database()) {
				clearDatabase_ResultSet = db.clearDatabaseCreateNewResultSet();
			} catch (Exception e) {
				e.printStackTrace();
				stringOut = e.toString() + "from ClearDatabase";
				throw new SQLException(stringOut);
			}

			//Anzahl der Spalten
			int lauf = 0;
			int rows = 0;
			try {
				clearDatabase_ResultSet.last();
				rows = clearDatabase_ResultSet.getRow();
				clearDatabase_ResultSet.beforeFirst();
			} catch (SQLException e) {
				e.printStackTrace();
				stringOut = e.toString() + "from ClearDatabase";
				throw new SQLException(stringOut);
			}

			String clearResult = "";

			while (clearDatabase_ResultSet.next() && lauf < rows) {
				clearResult = clearResult + clearDatabase_ResultSet.getString(1);
				lauf++;
			}

			//Ausgabe
			if (clearResult.equals("null")) {
				stringOut = stringOut + "<error>an bad error has happend (ClearDatabase)</error>";
			} else {
				stringOut = stringOut + "<input>database cleard and created a new one</input>";
			}
			return stringOut;
	}

    /**
     * Clear Database, SC
     *
     * @param _scname
	 * @return 
     * @throws SQLException
     */
    public static String ClearDatabaseSC(String _scname) throws SQLException {
			String StringOut = "";
            ///////////////////////////////////////////
            // Clear Database without Transformation //
            ///////////////////////////////////////////

            ResultSet ClearDatabase_ResultSet = null;

            try(Database db = new Database()) {
                ClearDatabase_ResultSet = db.clearDatabaseExceptTransformationResultSet(_scname);
            } catch (Exception e) {
                e.printStackTrace();
                StringOut = e.toString() + "from ClearDatabaseSC";
                throw new SQLException(StringOut);
            }

            //Anzahl der Spalten
            int lauf = 0;
            int rows = 0;
            try {
                ClearDatabase_ResultSet.last();
                rows = ClearDatabase_ResultSet.getRow();
                ClearDatabase_ResultSet.beforeFirst();
            } catch (SQLException e) {
                e.printStackTrace();
                StringOut = e.toString() + "from ClearDatabaseSC";
                throw new SQLException(StringOut);
            }

            String clearResult = "";

            while (ClearDatabase_ResultSet.next() && lauf < rows) {
                clearResult = clearResult + ClearDatabase_ResultSet.getString(1);
                lauf++;
            }

            //Ausgabe
            if (clearResult.equals("null")) {
                StringOut = StringOut + "<error>an bad error has happend (ClearDatabaseSC)</error>";
            } else {
                StringOut = StringOut + "<input>database cleard with project " + _scname + " except transfromation</input>";
            }

            ////////////////////////////////
            // Get all Transformation IDs //
            ////////////////////////////////

            ResultSet TransIDs_ResultSet = null;

            try(Database db = new Database()) {
                TransIDs_ResultSet = db.getTransformationIDsResultSet();
            } catch (Exception e) {
                e.printStackTrace();
                StringOut = e.toString() + "from ClearDatabaseSC";
                throw new SQLException(StringOut);
            }

            //Anzahl der Spalten
            lauf = 0;
            rows = 0;
            try {
                TransIDs_ResultSet.last();
                rows = TransIDs_ResultSet.getRow();
                TransIDs_ResultSet.beforeFirst();
            } catch (SQLException e) {
                e.printStackTrace();
                StringOut = e.toString() + "from ClearDatabaseSC";
                throw new SQLException(StringOut);
            }

            String transID = "";
            int[] ids = new int[rows];

            while (TransIDs_ResultSet.next() && lauf < rows) {
                transID = TransIDs_ResultSet.getString(1);
                ids[lauf] = Integer.parseInt(transID);
                lauf++;
            }

            //Ausgabe
            if (transID.equals("null")) {
                StringOut = StringOut + "<error>an bad error has happend (ClearDatabaseSC)</error>";
            } else {
                StringOut = StringOut + "<input>get transformation IDs</input>";
            }

            for (int i = 0; i < ids.length; i++) {

                ///////////////////////////////////
                // Delete unused Transformations //
                /// ////////////////////////////////

                ResultSet TransDelete_ResultSet = null;

                try(Database db = new Database()) {
                    TransDelete_ResultSet = db.deleteNotRequiredTransformationResultSet(ids[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                    StringOut = e.toString() + "from ClearDatabaseSC";
                    throw new SQLException(StringOut);
                }

                //Anzahl der Spalten
                lauf = 0;
                rows = 0;
                try {
                    TransDelete_ResultSet.last();
                    rows = TransDelete_ResultSet.getRow();
                    TransDelete_ResultSet.beforeFirst();
                } catch (SQLException e) {
                    e.printStackTrace();
                    StringOut = e.toString() + "from ClearDatabaseSC";
                    throw new SQLException(StringOut);
                }

                String deleteres = "";

                while (TransDelete_ResultSet.next() && lauf < rows) {
                    deleteres = deleteres + TransDelete_ResultSet.getString(1);
                    lauf++;
                }

                //Ausgabe
                if (deleteres.equals("null")) {
                    StringOut = StringOut + "<error>an bad error has happend (ClearDatabaseSC)</error>";
                } else if (deleteres.equals("f")) {
                    StringOut = StringOut + "<input>transformation " + ids[i] + " deleted</input>";
                }
            }

			return StringOut;
    }

}
