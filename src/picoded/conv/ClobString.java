package picoded.conv;

import java.lang.StringBuilder;
import java.lang.Character;
import oracle.sql.CLOB;

import java.sql.SQLException;
import java.io.IOException;
import java.io.BufferedReader;

/// Utility function that converst from oracle.sql.CLOB to string (and back!)
///
public class ClobString {

	static public String toString(CLOB inData) {
		try {
			return toStringNoisy(inData);
		} catch (SQLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	static public String toStringNoisy(CLOB inData) throws SQLException, IOException {
		final StringBuilder sb = new StringBuilder();
		final BufferedReader br = new BufferedReader(inData.getCharacterStream());
		String aux = br.readLine();
		if (aux != null) { //in case there is no data
			sb.append(aux);
			while ((aux = br.readLine()) != null) {
				sb.append("\n"); //append new line too
				sb.append(aux);
			}
		}
		br.close();
		return sb.toString();
	}
}