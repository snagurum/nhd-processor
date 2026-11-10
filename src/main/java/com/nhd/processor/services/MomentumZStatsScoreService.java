package com.nhd.processor.services;

import com.nhd.processor.models.MomentumZStatsScore;
import com.nhd.processor.repository.MomentumZStatsScoreRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.util.List;

@AllArgsConstructor
@Service
public class MomentumZStatsScoreService {
    private static final Logger log = LoggerFactory.getLogger(MomentumZStatsService.class);

    private MomentumZStatsScoreRepository momentumZStatsScoreRepository;

    @PersistenceContext
    private final EntityManager entityManager;

/**
    public void saveAll(List<MomentumZStatsScore> momentumZStatsScores){
        if (momentumZStatsScores == null || momentumZStatsScores.isEmpty()) return;
    
        List<LocalDate> distinctPriceDates = momentumZStatsScores.stream()
            .filter(java.util.Objects::nonNull)
            .map(MomentumZStatsScore::getPriceDate)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
            
        Map<String, MomentumZStatsScore> existingMap = getExistingMomentumZStatsScores(distinctPriceDates);

        Map<String, MomentumZStatsScore> workingBatchMap = new HashMap<>();

        momentumZStatsScores.stream().filter(java.util.Objects::nonNull).forEach(mzss -> {
            if (mzss.getTickerListName() == null || mzss.getPriceDate() == null || mzss.getTicker() == null) {
                return;
            }

            String key = mzss.getTickerListName() + "_" + mzss.getPriceDate() + "_" + mzss.getTicker();
            
            if (workingBatchMap.containsKey(key)) {
                updateExistingMomentumZStatsScores(workingBatchMap.get(key), mzss);
            } else if (existingMap.containsKey(key)) {
                workingBatchMap.put(key, updateExistingMomentumZStatsScores(existingMap.get(key), mzss));
            } else {
                workingBatchMap.put(key, mzss);
            }
        });

        if (!workingBatchMap.isEmpty()) {
            momentumZStatsScoreRepository.saveAll(workingBatchMap.values());
        }
    }

    public Map<String, MomentumZStatsScore> getExistingMomentumZStatsScores(List<LocalDate> priceDates){
        Map<String, MomentumZStatsScore> existingMap = new HashMap<>();
        if (priceDates == null || priceDates.isEmpty()) return existingMap;

        for (LocalDate priceDate : priceDates) {            
            List<MomentumZStatsScore> momentumZStatsScores = momentumZStatsScoreRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();            
            
            if (momentumZStatsScores != null) {
                Map<String, MomentumZStatsScore> tempMap = momentumZStatsScores.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(
                    mzss -> mzss.getTickerListName() + "_" + mzss.getPriceDate() + "_" + mzss.getTicker(),
                    mzss -> mzss,
                    (existing, replacement) -> existing
                ));
                existingMap.putAll(tempMap);
            }
        }
        return existingMap;
    }
    
    private MomentumZStatsScore updateExistingMomentumZStatsScores(MomentumZStatsScore oldMzss, MomentumZStatsScore newMzss){
        if( null != newMzss.getZ12())    oldMzss.setZ12 (newMzss.getZ12());
        if( null != newMzss.getZ6() )    oldMzss.setZ6  (newMzss.getZ6());
        if( null != newMzss.getZw())     oldMzss.setZw  (newMzss.getZw());
        if( null != newMzss.getNmr())    oldMzss.setNmr (newMzss.getNmr());
        return oldMzss;
    }
*/


    @Transactional
    public void saveAll(List<MomentumZStatsScore> momentumZStatsScores) {
        if (momentumZStatsScores == null || momentumZStatsScores.isEmpty()) {
            return;
        }

        entityManager.createNativeQuery("""
            CREATE TEMP TABLE temp_momentum_z_stats_score (
                 ticker_list_name            VARCHAR(20) NOT NULL,
                 price_date                  DATE NOT NULL,
                 ticker                      VARCHAR(20) NOT NULL,
                 z12                         NUMERIC(10, 2),
                 z6                          NUMERIC(10, 2),
                 zw                          NUMERIC(10, 2),
                 nmr                         NUMERIC(10, 2)
            ) ON COMMIT DROP;
        """).executeUpdate();

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "INSERT INTO temp_momentum_z_stats_score ( ticker_list_name, price_date, ticker, z12, z6, zw, nmr  ) "+
                            "values( ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchSize = 10000;
                int count = 0;

                for (MomentumZStatsScore mzss : momentumZStatsScores) {
                    ps.setString      ( 1, mzss.getTickerListName());
                    ps.setDate        ( 2, java.sql.Date.valueOf(mzss.getPriceDate()));
                    ps.setString      ( 3, mzss.getTicker());
                    ps.setBigDecimal  ( 4, mzss.getZ12());
                    ps.setBigDecimal  ( 5, mzss.getZ6());
                    ps.setBigDecimal  ( 6, mzss.getZw());
                    ps.setBigDecimal  ( 7, mzss.getNmr());
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
            INSERT INTO nse.momentum_z_stats_score ( ticker_list_name, price_date, ticker, z12, z6, zw, nmr  )
                select ticker_list_name, price_date, ticker, z12, z6, zw, nmr
                from temp_momentum_z_stats_score
                    ON CONFLICT (ticker_list_name, price_date, ticker)
                    DO UPDATE SET
                        z12 = COALESCE( EXCLUDED.z12 , momentum_z_stats_score.z12  ),
                        z6  = COALESCE( EXCLUDED.z6  , momentum_z_stats_score.z6   ),
                        zw  = COALESCE( EXCLUDED.zw  , momentum_z_stats_score.zw   ),
                        nmr = COALESCE( EXCLUDED.nmr , momentum_z_stats_score.nmr  );
            """).executeUpdate();

        entityManager.clear();
    }
}