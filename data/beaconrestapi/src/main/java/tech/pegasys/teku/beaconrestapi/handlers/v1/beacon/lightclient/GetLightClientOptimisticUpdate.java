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

import static tech.pegasys.teku.ethereum.json.types.EthereumTypes.ETH_CONSENSUS_HEADER_TYPE;
import static tech.pegasys.teku.ethereum.json.types.EthereumTypes.sszResponseType;
import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.SC_OK;
import static tech.pegasys.teku.infrastructure.http.RestApiConstants.TAG_BEACON;

import tech.pegasys.teku.api.ChainDataProvider;
import tech.pegasys.teku.api.DataProvider;
import tech.pegasys.teku.infrastructure.restapi.endpoints.EndpointMetadata;
import tech.pegasys.teku.infrastructure.restapi.endpoints.RestApiEndpoint;
import tech.pegasys.teku.spec.datastructures.lightclient.LightClientOptimisticUpdate;
import tech.pegasys.teku.spec.schemas.SchemaDefinitionCache;

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
}
