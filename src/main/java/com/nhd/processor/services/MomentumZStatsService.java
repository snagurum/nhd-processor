package com.nhd.processor.services;

import com.nhd.processor.models.MomentumZStats;
import com.nhd.processor.repository.MomentumZStatsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Service
public class MomentumZStatsService {
    private static final Logger log = LoggerFactory.getLogger(MomentumZStatsService.class);

    private MomentumZStatsRepository momentumZStatsRepository;

    @PersistenceContext
    private final EntityManager entityManager;


    public MomentumZStats getStockStatsForDate(LocalDate date, String tickerListName){
        return momentumZStatsRepository.findByPriceDateAndTickerListName(date,tickerListName  );
    }


/**
    public void saveAll(List<MomentumZStats> momentumZStats){
        if (momentumZStats == null || momentumZStats.isEmpty()) return;
    
        // 1. Safe extraction of distinct dates
        List<LocalDate> distinctPriceDates = momentumZStats.stream()
            .filter(java.util.Objects::nonNull)
            .map(MomentumZStats::getPriceDate)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
            
        Map<String, MomentumZStats> existingMap = getExistingMomentumZStats(distinctPriceDates);

        // 2. Track updates inside this loop instance to catch duplicate batch items
        Map<String, MomentumZStats> workingBatchMap = new HashMap<>();

        momentumZStats.stream().filter(java.util.Objects::nonNull).forEach(ss -> {
            if (ss.getTickerListName() == null || ss.getPriceDate() == null) {
                return; // Guard against incomplete records
            }

            String key = ss.getTickerListName() + "_" + ss.getPriceDate();
            
            if (workingBatchMap.containsKey(key)) {
                // If it's already in our working batch from a previous item in this loop, update it in place
                updateExistingMomentumZStats(workingBatchMap.get(key), ss);
            } else if (existingMap.containsKey(key)) {
                // If it exists in the DB lookup, update it and add it to our tracking batch
                workingBatchMap.put(key, updateExistingMomentumZStats(existingMap.get(key), ss));
            } else {
                // Brand new key entirely
                workingBatchMap.put(key, ss);
            }
        });

        // 3. Persist the uniquely mapped records
        if (!workingBatchMap.isEmpty()) {
            momentumZStatsRepository.saveAll(workingBatchMap.values());
        }
    }

    public Map<String, MomentumZStats> getExistingMomentumZStats(List<LocalDate> priceDates){
        Map<String, MomentumZStats> existingMap = new HashMap<>();
        if (priceDates == null || priceDates.isEmpty()) return existingMap;

        for (LocalDate priceDate : priceDates) {            
            List<MomentumZStats> momentumZStats = momentumZStatsRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();            
            
            if (momentumZStats != null) {
                Map<String, MomentumZStats> tempMap = momentumZStats.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(
                    ss -> ss.getTickerListName() + "_" + ss.getPriceDate(),
                    ss -> ss,
                    (existing, replacement) -> existing
                ));
                existingMap.putAll(tempMap);
            }
        }
        return existingMap;
    }
    
    private MomentumZStats updateExistingMomentumZStats(MomentumZStats oldMzs, MomentumZStats newMzs){
        if( null != newMzs.getMeanMr12())             oldMzs.setMeanMr12(newMzs.getMeanMr12());
        if( null != newMzs.getStdDevMr12() )          oldMzs.setStdDevMr12(newMzs.getStdDevMr12());
        if( null != newMzs.getMeanMr6())              oldMzs.setMeanMr6(newMzs.getMeanMr6());
        if( null != newMzs.getStdDevMr6())            oldMzs.setStdDevMr6(newMzs.getStdDevMr6());
        return oldMzs;
    }
*/


@Transactional
    public void saveAll(List<MomentumZStats> momentumZStats) {
        if (momentumZStats == null || momentumZStats.isEmpty()) {
            return;
        }

        entityManager.createNativeQuery("""
            CREATE TEMP TABLE temp_momentum_z_stats (
                ticker_list_name            VARCHAR(20) NOT NULL,
                price_date                  DATE NOT NULL,
                mean_mr12                   NUMERIC(10, 2),
                std_dev_mr12                NUMERIC(10, 2),
                mean_mr6                    NUMERIC(10, 2),
                std_dev_mr6                 NUMERIC(10, 2)
            ) ON COMMIT DROP;
        """).executeUpdate();

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "insert into temp_momentum_z_stats(ticker_list_name, price_date, mean_mr12, std_dev_mr12, mean_mr6, std_dev_mr6 )" +
               " values(?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchSize = 10000;
                int count = 0;

                for (MomentumZStats mzs : momentumZStats) {
                    ps.setString     ( 1, mzs.getTickerListName());
                    ps.setDate       ( 2, java.sql.Date.valueOf(mzs.getPriceDate()));
                    ps.setBigDecimal (3, mzs.getMeanMr12  ());
                    ps.setBigDecimal (4, mzs.getStdDevMr12());
                    ps.setBigDecimal (5, mzs.getMeanMr6   ());
                    ps.setBigDecimal (6, mzs.getStdDevMr6 ());
                    ps.addBatch();
                    if (++count % batchSize == 0) {
                        ps.executeBatch();
                    }
                }
                ps.executeBatch();
            }
        });
        entityManager.flush();

        entityManager.createNativeQuery("""
            insert into nse.momentum_z_stats(ticker_list_name, price_date, mean_mr12, std_dev_mr12, mean_mr6, std_dev_mr6 )
            select ticker_list_name, price_date, mean_mr12, std_dev_mr12, mean_mr6, std_dev_mr6
            from temp_momentum_z_stats
                ON CONFLICT (ticker_list_name, price_date)
                DO UPDATE SET
                    mean_mr12           = COALESCE( EXCLUDED.mean_mr12        , momentum_z_stats.mean_mr12        ),
                    std_dev_mr12        = COALESCE( EXCLUDED.std_dev_mr12     , momentum_z_stats.std_dev_mr12     ),
                    mean_mr6            = COALESCE( EXCLUDED.mean_mr6         , momentum_z_stats.mean_mr6         ),
                    std_dev_mr6         = COALESCE( EXCLUDED.std_dev_mr6      , momentum_z_stats.std_dev_mr6      );
            """).executeUpdate();

            entityManager.clear();
    }

}