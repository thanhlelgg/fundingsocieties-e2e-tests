package com.fundingsocieties.common;

public class Constants {
    public static final int LOW_TIMEOUT_IN_SECONDS = 2;
    public static final int MEDIUM_TIMEOUT_IN_SECONDS = 5;
    public static final int DEFAULT_TIME_WAIT = 10;
    public static final String TEST_RESULT_FOLDER = "test-results";
    public static final String COLLECTED_DATA_FOLDER = String.format("%s/funding-collected-data/%s", TEST_RESULT_FOLDER,
            DateTimeHelper.getCurrentTime("MM-dd-yyyy H-mm-ss"));
    public static final String STATISTIC_DETAILS_FILEPATH = COLLECTED_DATA_FOLDER + "/statistic-details.csv";
    public static final String FUNDING_APPROVED_FILEPATH = COLLECTED_DATA_FOLDER + "/funding-approved.csv";
    public static final String AMOUNT_DISBURSED_FILEPATH = COLLECTED_DATA_FOLDER + "/amount-disbursed.csv";
    public static final String DEFAULT_RATE_FILEPATH = COLLECTED_DATA_FOLDER + "/default-rate.csv";
    public static final String REPAYMENT_FILEPATH = COLLECTED_DATA_FOLDER + "/repayment.csv";
    public static final String INDUSTRY_DATA_FILEPATH = COLLECTED_DATA_FOLDER + "/industries.csv";
}
