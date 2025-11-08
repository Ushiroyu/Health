package com.community.health.common.util;

import java.util.List;

public record PageResponse<T>(long total, List<T> items) {}
