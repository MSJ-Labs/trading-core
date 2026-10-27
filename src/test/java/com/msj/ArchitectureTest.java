package com.msj;

import com.msj.shared.domain.ValueObject;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Boundary rules of the hexagonal layout (see CLAUDE.md). Each bounded context is split in
 * domain / application / infrastructure (plus api for its inbound web adapter). Ports are owned by the application
 * (application.port.out) and implemented by infrastructure adapters, so dependencies only point inwards.
 */
@AnalyzeClasses(packages = "com.msj", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsPureJava = classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..", "..domain..", "io.hypersistence.tsid..", "lombok..");

    // Spring is allowed here for composition only: no web, security or persistence types.
    // Temporary: marketdata still keeps its ports in infrastructure.ports; move them to application.port.out.
    @ArchTest
    static final ArchRule applicationDependsOnDomainOnly = classes()
            .that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..", "..domain..", "..application..", "lombok..", "org.slf4j..",
                    "org.springframework.stereotype..", "org.springframework.transaction.annotation..",
                    "com.msj.marketdata.infrastructure.ports..");

    @ArchTest
    static final ArchRule nothingInsideDependsOnTheOutside = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure.adapters..", "..api..", "..config..");

    // The bounded contexts only meet through the shared kernel: neither knows the other.
    @ArchTest
    static final ArchRule marketdataDoesNotDependOnIdentity = noClasses()
            .that().resideInAPackage("com.msj.marketdata..")
            .should().dependOnClassesThat().resideInAPackage("com.msj.identity..");

    @ArchTest
    static final ArchRule identityDoesNotDependOnMarketdata = noClasses()
            .that().resideInAPackage("com.msj.identity..")
            .should().dependOnClassesThat().resideInAPackage("com.msj.marketdata..");

    @ArchTest
    static final ArchRule jooqStaysInInfrastructure = noClasses()
            .that().resideOutsideOfPackage("..infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("org.jooq..");

    // A valueobject package holds true value objects only.
    @ArchTest
    static final ArchRule valueObjectPackagesHoldValueObjectsOnly = classes()
            .that().resideInAPackage("..valueobject..")
            .should().implement(ValueObject.class);
}