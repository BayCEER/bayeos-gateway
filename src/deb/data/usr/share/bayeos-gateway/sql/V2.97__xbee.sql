-- Out of range error in real_value() function 
create or replace function double2float(value float8)
returns float4 as 
$$
declare fvalue float4;
begin 
	begin 
		fvalue:= value::float4;
	exception 
		when others then fvalue:= 'NaN'::float4;
	end;
return fvalue;
end;
$$ language plpgsql;

create or replace function spline_value(adc float8, id int8)
 returns float8  
 LANGUAGE plperlu
AS $function$
	use strict;
	if (! defined $_[1]) {
		return $_[0];
	}
	if(! exists $_SHARED{"spline$_[1]"}){
		spi_exec_query("select init_spline($_[1])");
	}	
	if (@{$_SHARED{"spline$_[1]"}->[0]} > 0){
		return $_SHARED{"spline$_[1]"}->evaluate($_[0]);
	} else {
		return $_[0];
	}
	$function$
;

create or replace function real_value(adc float8, id int8) 
returns float4 as 
$$
begin
	return double2float(spline_value(adc,id));
end;
$$ language plpgsql;