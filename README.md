select
  book.site_code
  , book.user_id
  , sum(book.hk_total_amount)
  , sum(book.cx_total_amount)
  , sum(book.ns_total_amount)
from
  t_booking book 
where 
CAST(book.checkout_date as DATE) BETWEEN '2019-08-14 00:00:00' AND ’2019-09-14 00:00:00’
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
