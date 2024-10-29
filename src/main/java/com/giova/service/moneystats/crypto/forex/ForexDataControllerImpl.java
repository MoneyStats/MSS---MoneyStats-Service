package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/forex")
@CrossOrigin(origins = "*")
@Tag(name = "Forex", description = "API to handle Forex Data")
public class ForexDataControllerImpl implements ForexDataController {
  @Autowired private ForexDataService forexDataService;

  /**
   * API To get the forex data of a currency
   *
   * @param token User Access Token
   * @param currency To be searched
   * @return Forex Data
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getForexDataByCurrency(String token, String currency) {
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Forex Data For currency " + currency,
            TraceUtils.getSpanID(),
            forexDataService.getForexDataByCurrency(currency)));
  }

  /**
   * API to get the full list of Forex Data
   *
   * @param token User Access Token
   * @return Forex Data
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getForexDataList(String token) {
    List<ForexData> forex = forexDataService.getAllForexData();
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Founded " + forex.size() + " Forex Coins!",
            TraceUtils.getSpanID(),
            forex));
  }

  /**
   * API To Save the forex data
   *
   * @param token User Access Token
   * @param forexData To be saved
   * @return Forex Data Saved
   */
  @Override
  public ResponseEntity<Response> saveForexDataByCurrency(String token, ForexData forexData) {
    ForexData forex = forexDataService.saveForexData(forexData);
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Data for " + forex.getCurrency() + " Saved!",
            TraceUtils.getSpanID(),
            forex));
  }
}
