/* tslint:disable */
/* eslint-disable */
/**
 * AuraFrameFX Ecosystem API
 * A comprehensive API for interacting with the AuraFrameFX AI Super Dimensional Ecosystem. Provides access to generative AI capabilities, system customization, user management, and core application features. 
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: support@auraframefx.com
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


import * as runtime from '../runtime';
import type {
  ErrorResponse,
  User,
  UserPreferencesUpdate,
} from '../models/index';
import {
    ErrorResponseFromJSON,
    ErrorResponseToJSON,
    UserFromJSON,
    UserToJSON,
    UserPreferencesUpdateFromJSON,
    UserPreferencesUpdateToJSON,
} from '../models/index';

export interface UserPreferencesPutRequest {
    userPreferencesUpdate: UserPreferencesUpdate;
}

/**
 * UsersApi - interface
 * 
 * @export
 * @interface UsersApiInterface
 */
export interface UsersApiInterface {
    /**
     * 
     * @summary Get current user information
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApiInterface
     */
    userGetRaw(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<User>>;

    /**
     * Get current user information
     */
    userGet(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<User>;

    /**
     * 
     * @summary Update user preferences
     * @param {UserPreferencesUpdate} userPreferencesUpdate 
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     * @memberof UsersApiInterface
     */
    userPreferencesPutRaw(requestParameters: UserPreferencesPutRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<void>>;

    /**
     * Update user preferences
     */
    userPreferencesPut(requestParameters: UserPreferencesPutRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<void>;

}

/**
 * 
 */
export class UsersApi extends runtime.BaseAPI implements UsersApiInterface {

    /**
     * Get current user information
     */
    async userGetRaw(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<User>> {
        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        if (this.configuration && this.configuration.accessToken) {
            // oauth required
            headerParameters["Authorization"] = await this.configuration.accessToken("OAuth2AuthCode", ["profile"]);
        }

        const response = await this.request({
            path: `/user`,
            method: 'GET',
            headers: headerParameters,
            query: queryParameters,
        }, initOverrides);

        return new runtime.JSONApiResponse(response, (jsonValue) => UserFromJSON(jsonValue));
    }

    /**
     * Get current user information
     */
    async userGet(initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<User> {
        const response = await this.userGetRaw(initOverrides);
        return await response.value();
    }

    /**
     * Update user preferences
     */
    async userPreferencesPutRaw(requestParameters: UserPreferencesPutRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<runtime.ApiResponse<void>> {
        if (requestParameters['userPreferencesUpdate'] == null) {
            throw new runtime.RequiredError(
                'userPreferencesUpdate',
                'Required parameter "userPreferencesUpdate" was null or undefined when calling userPreferencesPut().'
            );
        }

        const queryParameters: any = {};

        const headerParameters: runtime.HTTPHeaders = {};

        headerParameters['Content-Type'] = 'application/json';

        if (this.configuration && this.configuration.accessToken) {
            // oauth required
            headerParameters["Authorization"] = await this.configuration.accessToken("OAuth2AuthCode", ["profile"]);
        }

        const response = await this.request({
            path: `/user/preferences`,
            method: 'PUT',
            headers: headerParameters,
            query: queryParameters,
            body: UserPreferencesUpdateToJSON(requestParameters['userPreferencesUpdate']),
        }, initOverrides);

        return new runtime.VoidApiResponse(response);
    }

    /**
     * Update user preferences
     */
    async userPreferencesPut(requestParameters: UserPreferencesPutRequest, initOverrides?: RequestInit | runtime.InitOverrideFunction): Promise<void> {
        await this.userPreferencesPutRaw(requestParameters, initOverrides);
    }

}
