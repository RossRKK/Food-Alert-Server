package server;


import java.sql.SQLException;
import java.util.ArrayList;

public class Record {
	private static ArrayList<Record> records = new ArrayList<Record>();
	
	private static final int MIN_CONCURENCY = 1;
	
	private int[] data;
	private String ean;
	private int concurs;
	
	//create a new record from a single source
	public Record(String ean, int[] data) {
		this.data = data;
		this.ean = ean;
		concurs = 0;
	}
	
	//increase the number of records that concur
	public void concurs() {
		concurs++;
	}
	
	/**
	 * Check whether enough users agree to actually add the data to the database
	 * @param ean The barcode number of the item in question
	 * @param data The newly submitted details
	 * @param dbm The database manger that is being used
	 * @throws SQLException
	 */
	public static void update(String ean, int[] data, DatabaseManager dbm) throws SQLException {
		//add this record if we don't have it
		if (!hasRecord(ean)) {
			Record r = new Record(ean, data);
			r.concurs --;
			records.add(r);
		}
		//find any record with a matching ean
		ArrayList<Record> matches = new ArrayList<Record>(); 
		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).ean.equals(ean)) {
				matches.add(records.get(i));
			}
		}
		
		//check whether the matches actually concur
		for (int i = 0; i < matches.size(); i++) {
			for (int j = 0; j < matches.get(i).data.length; j++) {
				if (matches.get(i).data[j] == data[j]) {
					matches.get(i).concurs();
					break;
				}
			}
		}
		
		//if there are enough concuring users add the record to the database
		for (int i = 0; i < matches.size(); i++) {
			if (matches.get(i).concurs >= MIN_CONCURENCY) {
				//push it to the database
				if (!dbm.exists(ean)) {
					dbm.add(ean, data);
					records.remove(matches.get(i));
				} else {
					dbm.update(ean, data);
					records.remove(matches.get(i));
				}
			}
		}
	}
	
	//create a new uncorroborated record
	public static void add(String ean, int[] data) {
		Record r = new Record(ean, data);
		records.add(r);
	}
	
	//check whether we have a record with a certain ean
	public static boolean hasRecord(String ean) {
		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).ean.equals(ean)) {
				return true;
			}
		}
		return false;
	}
}
