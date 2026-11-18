package com.nhd.processor.services;

import com.nhd.processor.dto.DataPage;
import com.nhd.processor.dto.DataQuery;
import com.nhd.processor.models.MomentumStats;
import com.nhd.processor.models.MomentumZStats;
import com.nhd.processor.models.MomentumZStatsScore;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MomentumStatsService {

    private final StocksDataClient client;

    public void saveMomentumStats(List<MomentumStats> rows) {
        client.save("/internal/momentum-stats/batch", rows);
    }

    public void saveMomentumZStats(List<MomentumZStats> rows) {
        client.save("/internal/momentum-z-stats/batch", rows);
    }

    public void saveMomentumZStatsScores(List<MomentumZStatsScore> rows) {
        client.save("/internal/momentum-z-scores/batch", rows);
    }

    public Page<MomentumStats> getMomentumStatsForDate(LocalDate date, List<String> tickers, Pageable pageable) {
        return client.page(
            "/internal/momentum-stats/date",
            new DataQuery(date, tickers, null, null, pageable.getPageNumber(), pageable.getPageSize()),
            pageable,
            new ParameterizedTypeReference<DataPage<MomentumStats>>() {}
        );
    }

    public MomentumZStats getMomentumZStatsForDate(LocalDate date, String tickerListName) {
        return client.post(
            "/internal/momentum-z-stats/date",
            new DataQuery(date, null, null, tickerListName, 0, 1),
            new ParameterizedTypeReference<OptionalResult>() {}
        ).value();
    }

    public record OptionalResult(MomentumZStats value) {}
}
