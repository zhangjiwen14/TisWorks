package com.function.zhang;

import com.function.zhang.common.dto.ScoringObjDto;
import com.function.zhang.common.dto.UserGroupDto;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * スコアリング要素作成：未来一か月分の予約データを取得、事前集計不可の項目を一件ずつ集計
 *
 */
public class MysqlInvest6 implements Serializable {

    @Getter
    @Setter
    private List<UserGroupDto> userAggregateDtoList;

    public static void main(String... args) throws IOException {
        try {
//            String sqlFile = args[0];
//            String sumFile = args[1];
            String sqlFile = "getFutureData_scoring.sql";
            String sumFile = "getSumData_scoring.sql";
            MysqlInvest6 aggregate = new MysqlInvest6();
            aggregate.execute(sqlFile, sumFile);
        }
        catch (ArrayIndexOutOfBoundsException | SQLException e){
            System.out.println(e);
        }
    }

    public void execute(String sqlFile, String sumFile) throws IOException, SQLException {
        int commitThreshold = 10000;
        int batchSize = 1000;
        long count = 0;

        log("batch size = %d", batchSize);
        log("commit threshold = %d", commitThreshold);

        long startTime = System.currentTimeMillis();
        long batchStartTime = System.currentTimeMillis();
        long transactionStartTime = System.currentTimeMillis();
//        String filesPath="/home/ugpf/data/sql/scoring/";
        String filesPath="C:\\Users\\ts-kimbun.cho\\Desktop\\UGPF\\sql\\";
        String query1 = new String(Files.readAllBytes(Paths.get(filesPath+sqlFile)), StandardCharsets.UTF_8);
        String query2 = new String(Files.readAllBytes(Paths.get(filesPath+sumFile)), StandardCharsets.UTF_8);
        {
            Connection connection = Mysql.createConnection();
            Connection connection_2 = Mysql.createConnection();
            // create the java statement
            Statement st = connection.createStatement();
            Statement st_2 = connection_2.createStatement();
            st.setFetchSize(Integer.MIN_VALUE);
            String user_id="";
            List<ScoringObjDto> s_list = new ArrayList<ScoringObjDto>();
            // execute the query, and get a java resultset
            try (ResultSet rs = st.executeQuery(query1)) {
                while (rs.next()) {
                    ScoringObjDto scoringObjDto = new ScoringObjDto();
                    scoringObjDto.setSiteCode(rs.getString("site_code"));
                    scoringObjDto.setBookingNo(rs.getString("booking_no"));
                    scoringObjDto.setCheckinDate(rs.getInt("checkin_date"));
                    scoringObjDto.setCheckoutDate(rs.getInt("checkout_date"));
                    scoringObjDto.setUserId(rs.getString("user_id"));
                    count++;
                    if (count % batchSize == 0) {
                        long lapTime = System.currentTimeMillis() - batchStartTime;
                        log("execute query = %d, lap time = %f sec", count, lapTime * 1.0 / 1000);
                        batchStartTime = System.currentTimeMillis();
                    }
                    if (count % commitThreshold == 0) {
                        long lapTime = System.currentTimeMillis() - transactionStartTime;
                        log("query(big) = %d, transaction time = %f sec", count, lapTime * 1.0 / 1000);
                        transactionStartTime = System.currentTimeMillis();
                    }
                    if (user_id.equals(scoringObjDto.getUserId())) {
                        for (ScoringObjDto s_s : s_list) {
                            if (scoringObjDto.getCheckinDate() <= s_s.getCheckoutDate() && scoringObjDto.getCheckoutDate() >= s_s.getCheckinDate()) {
                                // what we want
                                String www = s_s.getUserId();
                            }
                        }
                    } else {
                        s_list.clear();
                        String sq = query2.replace("@site_code@", scoringObjDto.getSiteCode()).replace("@user_id@", scoringObjDto.getUserId());
                        ResultSet rs_2 = st_2.executeQuery(sq);
                        while (rs_2.next()) {
                            ScoringObjDto s_s = new ScoringObjDto();
                            s_s.setUserId(rs_2.getString("user_id"));
                            s_s.setSiteCode(rs_2.getString("site_code"));
                            s_s.setBookingNo(rs_2.getString("booking_no"));
                            s_s.setCheckinDate(rs_2.getInt("checkin_date"));
                            s_s.setCheckoutDate(rs_2.getInt("checkout_date"));
                            s_list.add(s_s);
                        }
                        for (ScoringObjDto s_s : s_list) {
                            if (scoringObjDto.getCheckinDate() <= s_s.getCheckoutDate() && scoringObjDto.getCheckoutDate() >= s_s.getCheckinDate()) {
                                // what we want
                                String www = s_s.getUserId();
                            }
                        }
                        user_id = scoringObjDto.getUserId();
                    }
                }
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            log("data size = %s", count);
            log("execution time = %f sec", elapsedTime * 1.0 / 1000);
            st_2.close();
            st.close();
        }
    }

    void log(String format, Object... args) {
        System.out.printf("[%s] %s%n", LocalDateTime.now(), String.format(format, args));
    }
}
