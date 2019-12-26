"""Uploads an apk to the internal test track."""

from googleapiclient.discovery import build
from google.oauth2 import service_account
import argparse

"""python basic_upload_apks_service_account.py org.mozilla.firefox.vpn app-guardian-release-signed.apk private_key.json"""

TRACK = 'internal'  # Can be 'alpha', beta', 'production' or 'rollout'

# Declare command-line flags.
argparser = argparse.ArgumentParser(add_help=False)
argparser.add_argument('package_name',
                       help='The package name. Example: com.android.sample')
argparser.add_argument('apk_file',
                       nargs='?',
                       default='test.apk',
                       help='The path to the APK file to upload.')
argparser.add_argument('json_file',
                       help='json')
# argparser.add_argument('obb_file',
#                        help='The path to the obb file to upload.')


def main():
    # Process flags and read their values.
    flags = argparser.parse_args()

    credentials = service_account.Credentials.from_service_account_file(
        flags.json_file, scopes=['https://www.googleapis.com/auth/androidpublisher'])

    service = build('androidpublisher', 'v3', credentials=credentials)

    package_name = flags.package_name
    apk_file = flags.apk_file

    try:
        edit_request = service.edits().insert(body={}, packageName=package_name)
        result = edit_request.execute()
        edit_id = result['id']

        # print(f"Result: {result}")

        apk_response = service.edits().apks().upload(
            editId=edit_id,
            packageName=package_name,
            media_body=apk_file).execute()

        # print(apk_response)
        # print(f"Version code {apk_response['versionCode']} has been uploaded")

        track_response = service.edits().tracks().update(
            editId=edit_id,
            track=TRACK,
            packageName=package_name,
            body={'releases': [{
                  'versionCodes': [apk_response['versionCode']],
                  'status': u'completed'}]}).execute()

        # print(track_response)
        # print(f"Track {track_response['track']} is set with releases: {track_response['releases']}")

        commit_request = service.edits().commit(editId=edit_id, packageName=package_name).execute()

        # print(f"Edit {commit_request['id']} has been committed")

    except Exception as e:
        # print(f"Error: {e}")
        print("Erorr: %s" % e)
        raise "Upload Failed"


if __name__ == '__main__':
    main()
