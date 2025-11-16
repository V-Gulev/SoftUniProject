package com.fittrack.mainapp.service;

import com.fittrack.mainapp.model.dto.DashboardSummaryDto;

public interface DashboardService {
    DashboardSummaryDto buildDashboard(String username);
}

