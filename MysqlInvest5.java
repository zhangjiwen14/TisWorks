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
public class MysqlInvest5 implements Serializable {

    @Getter
    @Setter
    private List<UserGroupDto> userAggregateDtoList;

    /**
     * 初期化
     */
    public void init() {
        userAggregateDtoList = new ArrayList<>();
    }

    public static void main(String... args) throws IOException {
        try {
//            String sqlFile = args[0];
//            String sumFile = args[1];
            String sqlFile = "getFutureData_scoring.sql";
            String sumFile = "getSumData_scoring.sql";
            MysqlInvest5 aggregate = new MysqlInvest5();
            aggregate.init();
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
            // create the java statement
            Statement st = connection.createStatement();
            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query1);
            // iterate through the java resultset
            List<ScoringObjDto> scoringObjDtos = new ArrayList<ScoringObjDto>();
            while (rs.next())
            {
                ScoringObjDto scoringObjDto = new ScoringObjDto();
                scoringObjDto.setSiteCode(rs.getString("site_code"));
                scoringObjDto.setBookingNo(rs.getString("booking_no"));
                scoringObjDto.setCheckinDate(rs.getInt("checkin_date"));
                scoringObjDto.setCheckoutDate(rs.getInt("checkout_date"));
                scoringObjDto.setUserId(rs.getString("user_id"));
                scoringObjDtos.add(scoringObjDto);
            }
            String user_id_prev = "";
//            String facility_list = "";
            // csv file output
//            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filesPath+sqlFile.replace(".sql","_scoring.csv")));
            // write header line containing column names
//            fileWriter.write("user_id,facility_list");
            String user_id="";
            List<ScoringObjDto> s_list = new ArrayList<ScoringObjDto>();
            for(ScoringObjDto scoringObjDto : scoringObjDtos) {
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
                        if (scoringObjDto.getCheckinDate() <= s_s.getCheckoutDate() && scoringObjDto.getCheckoutDate() >= s_s.getCheckinDate()){
                            // what we want
                            String www = s_s.getUserId();
                        }
                    }
                    continue;
                }
                s_list.clear();
                String sq=query2.replace("@site_code@",scoringObjDto.getSiteCode()).replace("@user_id@", scoringObjDto.getUserId());
                ResultSet rs_1 = st.executeQuery(sq);
                while (rs_1.next()) {
                    ScoringObjDto s_s = new ScoringObjDto();
                    s_s.setUserId(rs_1.getString("user_id"));
                    s_s.setSiteCode(rs_1.getString("site_code"));
                    s_s.setBookingNo(rs_1.getString("booking_no"));
                    s_s.setCheckinDate(rs_1.getInt("checkin_date"));
                    s_s.setCheckoutDate(rs_1.getInt("checkout_date"));
                    s_list.add(s_s);
                }
                user_id = scoringObjDto.getUserId();

//                if (user_id_prev == userId) {
//                    // use facility_list queried previously
//                } else {
//                    count++;
//                    if (count % batchSize == 0) {
//                        long lapTime = System.currentTimeMillis() - batchStartTime;
//                        log("execute query = %d, lap time = %f sec", count, lapTime * 1.0 / 1000);
//                        batchStartTime = System.currentTimeMillis();
//                    }
//                    if (count % commitThreshold == 0) {
//                        long lapTime = System.currentTimeMillis() - transactionStartTime;
//                        log("query(big) = %d, transaction time = %f sec", count, lapTime * 1.0 / 1000);
//                        transactionStartTime = System.currentTimeMillis();
//                    }
//                    facility_list = "";
//                    String sq=query2.replace("@user_id@",userId);
//                    ResultSet rs_1 = st.executeQuery(sq);
//                    while (rs_1.next()) {
//                        String facility = rs_1.getString("user_id");
//                        facility_list += facility;
//                    }
//                    // here we can calculate the columns below with facility_list
//                    /**
//                     * ユーザーの1～360日以内の同一施設宿泊日数合計
//                     * ユーザーの1～720日以内の同一施設宿泊日数合計
//                     * ユーザーの前回宿泊からチェックイン日までの日数
//                     * ユーザーの同一日宿泊日数合計
//                     * ユーザーの同一日宿泊施設数合計
//                     */
////                    String[] summaryItems = getSummaryItems(userId, checkinDate, facility_list);
//
//                    String line = String.format("\"%s\",\"%s\"",userId,facility_list);
//
//                    fileWriter.newLine();
//                    fileWriter.write(line);
//
//                    user_id_prev = userId;
//                }
            }
//            fileWriter.close();
            long elapsedTime = System.currentTimeMillis() - startTime;
            log("data size = %s", count);
            log("execution time = %f sec", elapsedTime * 1.0 / 1000);

            st.close();
        }
    }

    void log(String format, Object... args) {
        System.out.printf("[%s] %s%n", LocalDateTime.now(), String.format(format, args));
    }
}
