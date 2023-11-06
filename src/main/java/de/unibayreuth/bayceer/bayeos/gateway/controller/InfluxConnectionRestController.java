package de.unibayreuth.bayceer.bayeos.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.exceptions.BadRequestException;
import com.influxdb.exceptions.ForbiddenException;

import de.unibayreuth.bayceer.bayeos.gateway.model.InfluxConnection;

@RestController
public class InfluxConnectionRestController {

    @Value("${INFLUX_ORG:bayeos}")
    private String influxOrg;

    final int OK = 0;
    final int PING_ERROR = 1;
    final int READ_ERROR = 2;
    final int WRITE_ERROR = 3;

    @RequestMapping(path = "/rest/influx-connection/test", method = RequestMethod.POST)
    public ResponseEntity testConnection(@RequestBody InfluxConnection ic) {

        InfluxDBClient c = null;
        try {
            // Create

            c = InfluxDBClientFactory.create(ic.getUrl(), ic.getToken().toCharArray(), influxOrg, ic.getBucket());
            // Ping

            if (!c.ping()) {
                return new ResponseEntity<Integer>(PING_ERROR, HttpStatus.OK);
            }
           

            // Read
            try {
                String query = String.format("from(bucket:\"%s\") |> range(start: -1m) |> limit(n:1)", ic.getBucket());
                c.getQueryApi().query(query);
            } catch (Exception e) {
                return new ResponseEntity<Integer>(READ_ERROR, HttpStatus.OK);
            }

            // Write
            try {
                WriteApiBlocking w = c.getWriteApiBlocking();
                w.writeRecord(WritePrecision.MS, "dummy");
            } catch (BadRequestException e) {
                return new ResponseEntity<Integer>(OK, HttpStatus.OK);
            } catch (ForbiddenException e) {
                return new ResponseEntity<Integer>(WRITE_ERROR, HttpStatus.OK);
            }

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // close the connection
            if (c != null) {
                c.close();
            }
        }
    }
}
