package com.example.mysql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

public class Importer {
    public static void main(String... args) throws SQLException, IOException, CsvValidationException {
        Importer importer = new Importer();
        importer.execute();
    }

    public void execute() throws SQLException, IOException, CsvValidationException {
        int commitThreshold = 100000;
        int batchSize = 1000;
        long count = 0;

        log("batch size = %d", batchSize);
        log("commit threshold = %d", commitThreshold);

        long startTime = System.currentTimeMillis();
        long batchStartTime = System.currentTimeMillis();
        long transactionStartTime = System.currentTimeMillis();

        try (CSVReader reader = new CSVReaderBuilder(Files.newBufferedReader(Paths.get("../../t_booking.csv"), StandardCharsets.UTF_8)).withSkipLines(1).build();
             Connection connection = Mysql.createConnection();
             PreparedStatement ps = connection.prepareCall("insert into t_booking(site_code, booking_no, login_time, booking_time, checkin_date, checkout_date, hk_days_count, cx_days_count, ns_days_count, guest_count, child_guest_count, checkin_time, traveltype_code, payment_code, hk_total_amount, cx_total_amount, ns_total_amount, user_id, member_code, domestin_user_code, name, telephonenumber, mailaddress, facility_no, facility_room_count, facility_prefecture_code) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            connection.setAutoCommit(false);

            String[] columns;
            while ((columns = reader.readNext()) != null) {
                count++;

                ps.setString(1, columns[0]); // site_code char(2)
                ps.setString(2, columns[1]); //,booking_no varchar(50)
                ps.setString(3, columns[2]); //,login_time datetime
                ps.setString(4, columns[3]); //,booking_time datetime
                ps.setInt(5, Integer.parseInt(columns[4])); //,checkin_date int(8)
                ps.setInt(6, Integer.parseInt(columns[5])); //,checkout_date int(8)
                ps.setInt(7, Integer.parseInt(columns[6])); //,hk_days_count int(3)
                ps.setInt(8, Integer.parseInt(columns[7])); //,cx_days_count int(3)
                ps.setInt(9, Integer.parseInt(columns[8])); //,ns_days_count int(3)
                ps.setInt(10, Integer.parseInt(columns[9])); //,guest_count int(3)
                ps.setInt(11, Integer.parseInt(columns[10])); //,child_guest_count int(3)
                ps.setString(12, columns[11]); //,checkin_time time
                ps.setString(13, columns[12]); //,traveltype_code char(2)
                ps.setString(14, columns[13]); //,payment_code char(2)
                ps.setInt(15, Integer.parseInt(columns[14])); //,hk_total_amount int(8)
                ps.setInt(16, Integer.parseInt(columns[15])); //,cx_total_amount int(8)
                ps.setInt(17, Integer.parseInt(columns[16])); //,ns_total_amount int(8)
                ps.setString(18, columns[17]); //,user_id varchar(50)
                ps.setString(19, columns[18]); //,member_code char(2)
                ps.setString(20, columns[19]); //,domestin_user_code char(2)
                ps.setString(21, columns[20]); //,name varchar(50)
                ps.setString(22, columns[21]); //,telephonenumber varchar(13)
                ps.setString(23, columns[22]); //,mailaddress varchar(100)
                ps.setString(24, columns[23]); //,facility_no varchar(50)
                ps.setInt(25, Integer.parseInt(columns[24])); //,facility_room_count int(5)
                ps.setString(26, columns[25]); //,facility_prefecture_code char(2)

                ps.addBatch();

                if (count % batchSize == 0) {
                    ps.executeBatch();

                    long lapTime = System.currentTimeMillis() - batchStartTime;
                    log("execute batch = %d, lap time = %f sec", count, lapTime * 1.0 / 1000);
                    batchStartTime = System.currentTimeMillis();
                }

                if (count % commitThreshold == 0) {
                    connection.commit();

                    long lapTime = System.currentTimeMillis() - transactionStartTime;
                    log("committed = %d, transaction time = %f sec", count, lapTime * 1.0 / 1000);
                    transactionStartTime = System.currentTimeMillis();
                }
            }

            ps.executeBatch();
            log("execute batch / %d", count);

            connection.commit();
            log("committed / %d", count);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        log("execution time = %f sec", elapsedTime * 1.0 / 1000);
    }

    void log(String format, Object... args) {
        System.out.printf("[%s] %s%n", LocalDateTime.now(), String.format(format, args));
    }
}
