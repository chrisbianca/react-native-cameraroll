// @flow
import { NativeModules } from 'react-native';

const RNCameraRollModule = NativeModules.RNCameraRoll;

export type AssetRequestType = "image" | "video" | "all";
export type AssetType = "image" | "video";

export type GetAssetsParams = {
  assetType: AssetRequestType;
  limit: number;
  start?: string | number;
}

export type Location = {
  altitude?: number;
  heading?: number;
  latitude: number;
  longitude: number;
  speed?: number;
}

export type Asset = {
  filename: string;
  height: number;
  location: Location;
  timestamp: number;
  type: AssetType;
  uri: string;
  width: number;
}

export type PageInfo = {
  end_cursor: string | number;
  has_next_page: boolean;
  start_cursor: string | number;
}

export type GetAssetsResponse = {
  assets: Array<Asset>;
  page_info: PageInfo;
}

/**
 * @class RNCameraRoll
 */
export default class RNCameraRoll {
  /**
   * @param params
   * @returns {Promise}
   */
  static getAssets(params: GetAssetsParams) : Promise<GetAssetsResponse> {
    return RNCameraRollModule.getAssets(params);
  }
}
