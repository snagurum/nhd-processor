package com.nhd.processor.dto;

import lombok.Data;

@Data
public class LoadBspTickerDTO {

    private Long id;

    private String ticker;

    private String priceDate;

    private String series;

    private String vwap;

    private String high52;

    private String low52;

    private String tradeClosePrice;

    private String ltp;

    private String tradeOpenPrice;

    private String tradePrevClosePrice;

    private String tradeHighPrice;

    private String tradeLowPrice;

    private String tradeValue;

    private String noOfTradeQty;

    private String noOfTrades;

}
