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
