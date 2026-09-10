/*
 * Copyright Consensys Software Inc., 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.beaconrestapi.handlers.v1.beacon.lightclient;

import static tech.pegasys.teku.beaconrestapi.handlers.v1.beacon.MilestoneDependentTypesUtil.getMultipleSchemaDefinitionFromMilestone;
import static tech.pegasys.teku.ethereum.json.types.EthereumTypes.*;
import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.SC_OK;
import static tech.pegasys.teku.infrastructure.http.RestApiConstants.TAG_BEACON;

import tech.pegasys.teku.api.ChainDataProvider;
import tech.pegasys.teku.api.DataProvider;
import tech.pegasys.teku.beaconrestapi.handlers.v1.beacon.MilestoneDependentTypesUtil;
import tech.pegasys.teku.infrastructure.json.types.SerializableTypeDefinition;
import tech.pegasys.teku.infrastructure.restapi.endpoints.EndpointMetadata;
import tech.pegasys.teku.infrastructure.restapi.endpoints.RestApiEndpoint;
import tech.pegasys.teku.spec.SpecMilestone;
import tech.pegasys.teku.spec.datastructures.lightclient.LightClientOptimisticUpdate;
import tech.pegasys.teku.spec.schemas.SchemaDefinitionCache;
import tech.pegasys.teku.spec.schemas.SchemaDefinitionsAltair;

import java.util.List;
import java.util.function.Function;

public class GetLightClientOptimisticUpdate extends RestApiEndpoint {
  public static final String ROUTE = "/eth/v1/beacon/light_client/optimistic_update";
  private final ChainDataProvider chainDataProvider;
  private final SchemaDefinitionCache schemaDefinitionCache;

  public GetLightClientOptimisticUpdate(
      final SchemaDefinitionCache schemaDefinitionCache, final DataProvider dataProvider) {
    return GetLightClientOptimisticUpdate(
        schemaDefinitionCache, dataProvider.getChainDataProvider());
  }

  public GetLightClientOptimisticUpdate(
      final SchemaDefinitionCache schemaDefinitionCache,
      final ChainDataProvider chainDataProvider) {
    super(
        EndpointMetadata.get(ROUTE)
            .operationId("getLightClientOptimisticUpdate")
            .summary("Get the latest known `LightClientOptimisticUpdate`")
            .description(
                "Requests the latest `LightClientOptimisticUpdate` known by the server. Depending on the `Accept` header it can be returned either as JSON or SSZ-serialized bytes.")
            .tags(TAG_BEACON)
            .response(
                SC_OK,
                "Request successful",
                getResponseType(schemaDefinitionCache),
                sszResponseType(
                    (final LightClientOptimisticUpdate optimisticUpdate) ->
                        milestoneAtOptimisticSlot(schemaDefinitionCache, optimisticUpdate)),
                ETH_CONSENSUS_HEADER_TYPE)
            .withNotFoundResponse()
            .withNotAcceptableResponse()
            .build());
    this.schemaDefinitionCache = schemaDefinitionCache;
    this.chainDataProvider = chainDataProvider;
  }

  private static SerializableTypeDefinition<LightClientOptimisticUpdate> getResponseType(final SchemaDefinitionCache schemaDefinitionCache) {
    final SerializableTypeDefinition<LightClientOptimisticUpdate> lightClientOptimisticUpdateType = getMultipleSchemaDefinitionFromMilestone(schemaDefinitionCache,
            "LightClientOptimisticUpdate", List.of(new MilestoneDependentTypesUtil.ConditionalSchemaGetter<>((optimisticUpdate, milestone) -> milestoneAtOptimisticSlot(schemaDefinitionCache, optimisticUpdate).equals(milestone),
    SpecMilestone.ALTAIR, schemaDefinitions -> SchemaDefinitionsAltair.required(schemaDefinitions).getLightClientOptimisticUpdateSchema())));

    return SerializableTypeDefinition.<LightClientOptimisticUpdate>object()
            .name("GetLightClientOptimisticUpdateResponse")
            .withField(
                    "version",
                    MILESTONE_TYPE,
                    optimisticUpdate -> milestoneAtOptimisticSlot(schemaDefinitionCache, optimisticUpdate)
            ).withField("data", lightClientOptimisticUpdateType, Function.identity())
            .build();
  }

  private static SpecMilestone milestoneAtOptimisticSlot(final SchemaDefinitionCache schemaDefinitionCache, final LightClientOptimisticUpdate optimisticUpdate) {
    return schemaDefinitionCache.milestoneAtSlot(optimisticUpdate.getAttestedHeader().getBeacon().getSlot());
  }
}
