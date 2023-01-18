package com.giova.service.moneystats.app;

import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Logged
@Service
public class AppService {

    public ResponseEntity<Response> getDashboardData() {


        return null;
    }
}
