package com.ai.baby.sqlagent.guard;

import com.ai.baby.sqlagent.domain.GuardResult;

public interface SqlGuard {

    GuardResult check(String sql);
}
