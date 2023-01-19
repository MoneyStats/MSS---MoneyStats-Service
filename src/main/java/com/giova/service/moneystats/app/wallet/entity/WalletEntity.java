package com.giova.service.moneystats.app.wallet.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.generic.GenericEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "WALLET")
public class WalletEntity extends GenericEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "CATEGORY", nullable = false)
    private String category;

    @Column(name = "BALANCE")
    private Double balance;

    @Column(name = "IMG", nullable = false)
    private String img;

    @Column(name = "ALL_TIME_HIGH")
    private Double allTimeHigh;

    @Column(name = "ALL_TIME_HIGH_DATE")
    private LocalDate allTimeHighDate;

    @Column(name = "HIGH_PRICE")
    private Double highPrice;

    @Column(name = "HIGH_PRICE_DATE")
    private LocalDate highPriceDate;

    @Column(name = "LOW_PRICE")
    private Double lowPrice;

    @Column(name = "LOW_PRICE_DATE")
    private LocalDate lowPriceDate;

    @Column(name = "PERFORMANCE_LAST_STATS")
    private Double performanceLastStats;

    @Column(name = "DIFFERENCE_LAST_STATS")
    private Double differenceLastStats;

    @Column(name = "DATE_LAST_STATS")
    private LocalDate dateLastStats;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<StatsEntity> history;
}
