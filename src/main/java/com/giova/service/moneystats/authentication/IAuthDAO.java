package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuthDAO extends JpaRepository<UserEntity, Long> {

    UserEntity findUserEntityByUsernameOrEmail(String username, String email);

    UserEntity findUserEntityByEmail(String email);

    UserEntity findUserEntityByTokenReset(String token);


    /**
     *  Select the Crypto Fiat Currency to be used to import the currency
     * @return
     */
    @Query(
            value =
                    "select distinct USER.cryptoCurrency from UserEntity USER")
    List<String> selectDistinctCryptoFiatCurrency();
}
