package com.nhd.processor.services;

import com.nhd.processor.models.MomentumStats;
import com.nhd.processor.repository.MomentumStatsRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Service
public class MomentumStatsService {
    private static final Logger log = LoggerFactory.getLogger(MomentumStatsService.class);

    private MomentumStatsRepository momentumStatsRepository;

    @PersistenceContext
    private final EntityManager entityManager;


    public Page<MomentumStats> getByTicker(String ticker, LocalDate fromDate, Pageable pageable){
        if(fromDate == null)
            return momentumStatsRepository.findByTickerOrderByPriceDate(ticker, pageable);
        else return momentumStatsRepository.findByTickerAndPriceDateGreaterThanEqualOrderByPriceDateAsc(ticker, fromDate, pageable);
    }

    public Page<MomentumStats> findByPriceDate(LocalDate priceDate, Pageable pageable){
        return momentumStatsRepository.findByPriceDate(priceDate, pageable);
    }

    public Page<MomentumStats> getMomentumStatsForDate(LocalDate date, List<String> tickers, Pageable pageable){
        Page<MomentumStats> momentumStatsPage;
        if(null == tickers || tickers.size() == 0 || "***ALL***".equals(tickers.get(0)))
            momentumStatsPage = momentumStatsRepository.findByPriceDate(date, pageable);
        else if (tickers.size() > 1)
            momentumStatsPage = momentumStatsRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
        else
            momentumStatsPage = momentumStatsRepository.findByPriceDateAndTickerStartingWith(date, ""+tickers.get(0).charAt(0), pageable);
        return momentumStatsPage;
    }

    public Page<MomentumStats> findByPriceDateAndTickerStartingWith(LocalDate date, String ticker, Pageable pageable){
        return momentumStatsRepository.findByPriceDateAndTickerStartingWith(date, ticker, pageable);
    }

    public Page<MomentumStats> findByPriceDateAndTickerIn(LocalDate date, List<String> tickers, Pageable pageable){
        return momentumStatsRepository.findByPriceDateAndTickerIn(date, tickers, pageable);
    }

/**
    public void saveAll(List<MomentumStats> momentumStats){
        if (momentumStats == null || momentumStats.isEmpty()) return;
    
        List<MomentumStats> toBeMomentumStats = new ArrayList<>();
        List<LocalDate> distinctPriceDates = momentumStats.stream()
            .map(MomentumStats::getPriceDate)
            .distinct()
            .collect(Collectors.toList());
        Map<String, MomentumStats> existingMap = getExistingMomentumStats(distinctPriceDates);

        momentumStats.forEach(ss -> {
            String key = ss.getTicker() + "_" + ss.getPriceDate();
            if (existingMap.containsKey(key)) {
                toBeMomentumStats.add(updateExistingMomentumStats(existingMap.get(key), ss));
            } else {
                toBeMomentumStats.add(ss);
            }
        });
        momentumStatsRepository.saveAll(toBeMomentumStats);
    }


    public Map<String, MomentumStats> getExistingMomentumStats(List<LocalDate> priceDates){

        Map<String, MomentumStats> existingMap = new HashMap<>();
        for (LocalDate priceDate : priceDates) {            
            List<MomentumStats> momentumStats = momentumStatsRepository.findByPriceDate(
                priceDate,
                null
            ).getContent();            
            Map<String, MomentumStats> tempMap = momentumStats.stream()
            .collect(Collectors.toMap(
                ss -> ss.getTicker() + "_" + ss.getPriceDate(),
                ss -> ss,
                (existing, replacement) -> existing
            ));
            existingMap.putAll(tempMap);
        }
        return existingMap;
    }


    private MomentumStats updateExistingMomentumStats(MomentumStats oldMs, MomentumStats newMs){
		if( null != newMs.getDailyReturn())             oldMs.setDailyReturn(newMs.getDailyReturn());
		if( null != newMs.getLnDailyReturn())           oldMs.setLnDailyReturn(newMs.getLnDailyReturn());

		if( null != newMs.getMean1yrLnDailyReturn())    oldMs.setMean1yrLnDailyReturn(newMs.getMean1yrLnDailyReturn());
		if( null != newMs.getSum1yrLnDailyReturn())     oldMs.setSum1yrLnDailyReturn(newMs.getSum1yrLnDailyReturn());
		if( null != newMs.getN1yr())                    oldMs.setN1yr(newMs.getN1yr());
		if( null != newMs.getStdDev1yrLnDailyReturn())  oldMs.setStdDev1yrLnDailyReturn(newMs.getStdDev1yrLnDailyReturn());
		if( null != newMs.getStdDev1yrAnnualized())     oldMs.setStdDev1yrAnnualized(newMs.getStdDev1yrAnnualized());
		if( null != newMs.getPriceReturn1yr())          oldMs.setPriceReturn1yr(newMs.getPriceReturn1yr());
		if( null != newMs.getMr12())                    oldMs.setMr12(newMs.getMr12());
		
		if( null != newMs.getMean6mLnDailyReturn())    oldMs.setMean6mLnDailyReturn(newMs.getMean6mLnDailyReturn());
		if( null != newMs.getSum6mLnDailyReturn())      oldMs.setSum1yrLnDailyReturn(newMs.getSum6mLnDailyReturn());
		if( null != newMs.getN6m())                    oldMs.setN6m(newMs.getN6m());
		if( null != newMs.getStdDev6mLnDailyReturn())  oldMs.setStdDev6mLnDailyReturn(newMs.getStdDev6mLnDailyReturn());
		if( null != newMs.getStdDev6mAnnualized())     oldMs.setStdDev6mAnnualized(newMs.getStdDev6mAnnualized());
		if( null != newMs.getPriceReturn6m())          oldMs.setPriceReturn6m(newMs.getPriceReturn6m());
		if( null != newMs.getMr6())                    oldMs.setMr6(newMs.getMr6());
        return oldMs;
    }
*/

    @Transactional
    public void saveAll(List<MomentumStats> momentumStats) {
        if (momentumStats == null || momentumStats.isEmpty()) {
            return;
        }

        entityManager.createNativeQuery("""
            CREATE TEMP TABLE temp_momentum_stats (
                ticker                      VARCHAR(20) NOT NULL,
                price_date                  DATE NOT NULL,
                daily_return                NUMERIC(10, 2),
                ln_daily_return             NUMERIC(10, 2),
                mean_1yr_ln_daily_return    NUMERIC(10, 2),
                sum_1yr_ln_daily_return     NUMERIC(10, 2),
                n_1yr                       NUMERIC(10),
                std_dev_1yr_ln_daily_return NUMERIC(10, 2),
                std_Dev_1yr_Annualized      NUMERIC(10, 2),
                price_return_1yr            NUMERIC(10, 2),
                mr12                        NUMERIC(10, 2),
                mean_6m_ln_daily_return     NUMERIC(10, 2),
                sum_6m_ln_daily_return      NUMERIC(10, 2),
                n_6m                        NUMERIC(10, 2),
                std_dev_6m_ln_daily_return  NUMERIC(10, 2),
                std_dev_6m_annualized       NUMERIC(10, 2),
                price_return_6m             NUMERIC(10, 2),
                mr6                         NUMERIC(10, 2)
            ) ON COMMIT DROP;
        """).executeUpdate();

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "INSERT INTO temp_momentum_stats ( ticker, price_date, daily_return, ln_daily_return, mean_1yr_ln_daily_return, sum_1yr_ln_daily_return, n_1yr, std_dev_1yr_ln_daily_return, std_Dev_1yr_Annualized, price_return_1yr, mr12, mean_6m_ln_daily_return, sum_6m_ln_daily_return, n_6m, std_dev_6m_ln_daily_return, std_dev_6m_annualized, price_return_6m, mr6) "+
                            "values( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchSize = 10000;
                int count = 0;

                for (MomentumStats ms : momentumStats) {
                    ps.setString      ( 1, ms.getTicker());
                    ps.setDate        ( 2, java.sql.Date.valueOf(ms.getPriceDate()));
                    ps.setBigDecimal  ( 3, ms.getDailyReturn());
                    ps.setBigDecimal  ( 4, ms.getLnDailyReturn());
                    ps.setBigDecimal  ( 5, ms.getMean1yrLnDailyReturn());
                    ps.setBigDecimal  ( 6, ms.getSum1yrLnDailyReturn());
                    ps.setObject      ( 7, ms.getN1yr());
                    ps.setBigDecimal  ( 8, ms.getStdDev1yrLnDailyReturn());
                    ps.setBigDecimal  ( 9, ms.getStdDev1yrAnnualized());
                    ps.setBigDecimal  (10, ms.getPriceReturn1yr());
                    ps.setBigDecimal  (11, ms.getMr12());
                    ps.setBigDecimal  (12, ms.getMean6mLnDailyReturn());
                    ps.setBigDecimal  (13, ms.getSum6mLnDailyReturn());
                    ps.setObject      (14, ms.getN6m());
                    ps.setBigDecimal  (15, ms.getStdDev6mLnDailyReturn());
                    ps.setBigDecimal  (16, ms.getStdDev6mAnnualized());
                    ps.setBigDecimal  (17, ms.getPriceReturn6m());
                    ps.setBigDecimal  (18, ms.getMr6());
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
            INSERT INTO nse.momentum_stats ( ticker, price_date, daily_return, ln_daily_return, mean_1yr_ln_daily_return, sum_1yr_ln_daily_return, n_1yr, std_dev_1yr_ln_daily_return, std_Dev_1yr_Annualized, price_return_1yr, mr12, mean_6m_ln_daily_return, sum_6m_ln_daily_return, n_6m, std_dev_6m_ln_daily_return, std_dev_6m_annualized, price_return_6m, mr6)
                select ticker, price_date, daily_return, ln_daily_return, mean_1yr_ln_daily_return, sum_1yr_ln_daily_return, n_1yr, std_dev_1yr_ln_daily_return, std_Dev_1yr_Annualized, price_return_1yr, mr12, mean_6m_ln_daily_return, sum_6m_ln_daily_return, n_6m, std_dev_6m_ln_daily_return, std_dev_6m_annualized, price_return_6m, mr6
                from temp_momentum_stats
            ON CONFLICT (ticker, price_date)
            DO UPDATE SET
                daily_return                = COALESCE( EXCLUDED.daily_return                  , momentum_stats.daily_return                  ),
                ln_daily_return             = COALESCE( EXCLUDED.ln_daily_return               , momentum_stats.ln_daily_return               ),
                mean_1yr_ln_daily_return    = COALESCE( EXCLUDED.mean_1yr_ln_daily_return      , momentum_stats.mean_1yr_ln_daily_return      ),
                sum_1yr_ln_daily_return     = COALESCE( EXCLUDED.sum_1yr_ln_daily_return       , momentum_stats.sum_1yr_ln_daily_return       ),
                n_1yr                       = COALESCE( EXCLUDED.n_1yr                         , momentum_stats.n_1yr                         ),
                std_dev_1yr_ln_daily_return = COALESCE( EXCLUDED.std_dev_1yr_ln_daily_return   , momentum_stats.std_dev_1yr_ln_daily_return   ),
                std_Dev_1yr_Annualized      = COALESCE( EXCLUDED.std_Dev_1yr_Annualized        , momentum_stats.std_Dev_1yr_Annualized        ),
                price_return_1yr            = COALESCE( EXCLUDED.price_return_1yr              , momentum_stats.price_return_1yr              ),
                mr12                        = COALESCE( EXCLUDED.mr12                          , momentum_stats.mr12                          ),
                mean_6m_ln_daily_return     = COALESCE( EXCLUDED.mean_6m_ln_daily_return       , momentum_stats.mean_6m_ln_daily_return       ),
                sum_6m_ln_daily_return      = COALESCE( EXCLUDED.sum_6m_ln_daily_return        , momentum_stats.sum_6m_ln_daily_return        ),
                n_6m                        = COALESCE( EXCLUDED.n_6m                          , momentum_stats.n_6m                          ),
                std_dev_6m_ln_daily_return  = COALESCE( EXCLUDED.std_dev_6m_ln_daily_return    , momentum_stats.std_dev_6m_ln_daily_return    ),
                std_dev_6m_annualized       = COALESCE( EXCLUDED.std_dev_6m_annualized         , momentum_stats.std_dev_6m_annualized         ),
                price_return_6m             = COALESCE( EXCLUDED.price_return_6m               , momentum_stats.price_return_6m               ),
                mr6                         = COALESCE( EXCLUDED.mr6                           , momentum_stats.mr6                           );
            """).executeUpdate();

        entityManager.clear();
    }
}
