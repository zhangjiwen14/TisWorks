java -jar /home/ugpf/appl/ugpf-investigation/target/azure-functions/ugpf-investigation-1618063138903/ugpf-investigation-1.0-SNAPSHOT.jar 'testcase1.sql'


select
    book.site_code
     , book.user_id
     , sum(book.hk_total_amount)
     , sum(book.cx_total_amount)
     , sum(book.ns_total_amount)
from
    t_booking book
where
    CAST(book.checkout_date as DATE) BETWEEN '2019-08-14 00:00:00' AND '2019-08-14 00:00:00'
group by
    book.site_code
        , book.user_id;
  
 select book.facility_no
  , sum(book.hk_total_amount)
  , sum(book.cx_total_amount)
  , sum(book.ns_total_amount)
from t_booking book 
where  
CAST(book.checkout_date as DATE) BETWEEN '2019-08-14 00:00:00' AND ’2019-09-14 00:00:00’
group by
  book.facility_no;


select book.site_code
  , sum(book.hk_total_amount)
  , sum(book.cx_total_amount)
  , sum(book.ns_total_amount)
from t_booking book
where  
CAST(book.checkout_date as DATE) BETWEEN '2019-08-14 00:00:00' AND ’2019-09-14 00:00:00’
group by
  book.site_code;




insert into sum_userid (
    site_code,user_id,hk_total_amount,cx_total_amount,ns_total_amount
)
select   book.site_code
     , book.user_id
     , sum(book.hk_total_amount)
     , sum(book.cx_total_amount)
     , sum(book.ns_total_amount)
from
    t_booking book
where CAST(book.checkout_date as DATE) BETWEEN '2019-08-14 00:00:00' AND '2019-09-14 00:00:00'
group by
    book.site_code
       , book.user_id;

select book.booking_no,
       score.hk_total_amount,
       score.cx_total_amount,
       score.ns_total_amount
from
    t_booking book
inner join
    sum_userid score
    on book.site_code = score.site_code
    and book.user_id = score.user_id;







explain select book.site_code, 
book.user_id,
score.user_hk_count, 
score.user_cx_count, 
score.user_ns_count
from 
t_booking book
left join 
(select
  book.site_code
  , book.user_id
  , sum(book.hk_total_amount) user_hk_count
  , sum(book.cx_total_amount) user_cx_count
  , sum(book.ns_total_amount) user_ns_count
from
  t_booking book 
where book.checkout_date >=20190814 and book.checkout_date <= 20190914
group by
  book.site_code
  , book.user_id) score
  on book.site_code = score.site_code
  and book.user_id = score.user_id
where book.checkout_date >=20190814 and book.checkout_date <= 20190914
order by
  1
  , 2;



select
  site_code,
  user_id,
  sum(hk_days_count),  -- 宿泊日数
  sum(hk_total_amount),  -- 宿泊金額
  sum(case when hk_days_count >= 1 then guest_count else 0 end)  -- 利用人数
from
  t_booking
where
  checkin_date >= 20191014 and checkin_date < 20191114
group by
  site_code,
  user_id;



・集計単位でGroup By → 集計単位ごとにデータ抽出＋プログラム上で集計（もともとのアプリ案） → 集計単位数だけ繰り返し
・期間区切りで絞り込み＋集計単位でGroup By＋集合演算 → 集計単位数だけ繰り返し
・期間区切りで絞り込み → プログラム上で集計（全集計単位、いっきに実施。ユーザー以外は期間内の全データをメモリ上で合算でよい）


	• VM名： ugpf-tec-ssh-access
	• DNS名： ugpf-tec-ssh-access.japaneast.cloudapp.azure.com
	• 23時に自動シャットダウン
	• 接続ユーザー名： azureuser
	• SSH公開鍵： 

	• 接続コマンドイメージ
ssh azureuser@ugpf-tec-ssh-access.japaneast.cloudapp.azure.com -i ugpf-tec-ssh-access.pem -p 443 -o ProxyCommand='nc -X connect -x tkyproxy.intra.tis.co.jp:8080 %h %p'



select
  * 
from
  t_scoring_objects 
where
  site_code = '@site_code@' 
  and user_id = '@user_id@' 
  and @checkin_date@<= checkout_date 
  and @checkout_date@>= checkin_date 
  and payment_code = '01'


mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms2048m -Xmx4096m -Dspring.profiles.active=local -Dbatch.id=sample01"

java -jar -Xms2048m -Xmx4096m -Dspring.profiles.active=local -Dbatch.id=sample01 lib/batch-0.0.1-SNAPSHOT.jar

java -cp lib/*:conf/:batch-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=local -Dbatch.id=sample01 batch.Bootstrap



mkdir azagent;cd azagent;curl -fkSL -o vstsagent.tar.gz https://vstsagentpackage.azureedge.net/agent/2.187.2/vsts-agent-linux-x64-2.187.2.tar.gz;tar -zxvf vstsagent.tar.gz; if [ -x "$(command -v systemctl)" ]; then ./config.sh --environment --environmentname "avmdeployment" --acceptteeeula --agent $HOSTNAME --url https://dev.azure.com/booking-brain/ --work _work --projectname 'booking-brain' --auth PAT --token pbpdb34i7gyuzqmbhyv674yudfof47e4mvbg6ixuto5u5aahby5q --runasservice; sudo ./svc.sh install; sudo ./svc.sh start; else ./config.sh --environment --environmentname "avmdeployment" --acceptteeeula --agent $HOSTNAME --url https://dev.azure.com/booking-brain/ --work _work --projectname 'booking-brain' --auth PAT --token pbpdb34i7gyuzqmbhyv674yudfof47e4mvbg6ixuto5u5aahby5q; ./run.sh; fi
