// @flow
import { NativeModules } from 'react-native';

const RNCameraRollModule = NativeModules.RNCameraRoll;

type AssetType = "image" | "video" | "all";

type GetAssetsParams = {
  assetType?: AssetType;
  first: number;
  start?: string | number;
}

type Location = {
  altitude?: number;
  heading?: number;
  latitude: number;
  longitude: number;
  speed?: number;
}

type Asset = {
  filename: string;
  height: number;
  location: Location;
  timestamp: number;
  type: AssetType;
  uri: string;
  width: number;
}

type PageInfo = {
  end_cursor: string | number;
  has_next_page: boolean;
  start_cursor: string | number;
}

type GetAssetsResponse = {
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
