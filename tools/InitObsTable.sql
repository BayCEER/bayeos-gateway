delete from observation;
delete from observation_calc ;
delete from observation_exp ;
delete from observation_out ;
update channel set last_result_time = null, last_result_value = null ;


