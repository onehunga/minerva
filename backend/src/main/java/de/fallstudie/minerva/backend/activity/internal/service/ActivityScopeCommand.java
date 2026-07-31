package de.fallstudie.minerva.backend.activity.internal.service;

import de.fallstudie.minerva.backend.activity.ActivityScopeType;

public record ActivityScopeCommand(ActivityScopeType type, long id) {
}
