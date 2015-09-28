#!/usr/bin/env ruby
require 'thor'
require 'logger'
require 'pathname'
require 'json'

class CLI < Thor
  class_option :verbose, type: :boolean, default: false

  desc 'run_instance', 'Run new instance'
  option :project, default: 'isucon5-qualify'
  option :name, required: true
  option :zone, default: 'asia-east1-b'
  option :machine_type, default: 'n1-highcpu-4'
  option :network, default: 'default'
  option :maintenance_policy, default: 'TERMINATE'
  option :scopes, default: ['https://www.googleapis.com/auth/devstorage.read_write', 'https://www.googleapis.com/auth/logging.write']
  option :tags, default: ['http-server']
  option :image, default: 'https://www.googleapis.com/compute/v1/projects/ubuntu-os-cloud/global/images/ubuntu-1504-vivid-v20150616a'
  option :boot_disk_size, default: 16
  option :boot_disk_type, default: 'pd-standard'
  option :boot_disk_device_name, default: 'base1'
  option :root_ssh_key, default: 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCwttspGr+7FLz0f6d/Cfd2o5MzQ11+0/De7Kh2bm6XLfu7M2cRk3KL8FuvJ8a8ZgAbB/PiUEDRaqXo2UU54gzJYOmlILM7yu8J/U2iJrIDK0MPc53xLJHzk+PzBRqWvVsW2PwsIqykV7sgRVgIHc4wN7+OZzdiTdKe5wnfWxYs4jx46MAxkfv81fk2FHekqf4P/dZUIJBtS1UiCjw8O2cuaGHWkMHYLUeo18PR3yQR9zKW/5cxhYXTArNRtQBwTLXQtQCh/EDYjGwnEqs/KY+Vss4933pET2HphnzG2m32t0YCQ4Epb9sAwxB0aqxN3IPD0kLtVpzMgOHG1pyXdhQ5 root@base-image.isucon.net'
  def run_instance
    args = options.merge({
      no_restart_on_failure: nil,
      no_boot_disk_auto_delete: nil,
    })
    project = args.delete(:project)
    name = args.delete(:name)

    metadata = {}
    metadata[:sshKeys] = 'root:' + args.delete(:root_ssh_key)

    # --metadata "sshKeys=root:ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCwttspGr+7FLz0f6d/Cfd2o5MzQ11+0/De7Kh2bm6XLfu7M2cRk3KL8FuvJ8a8ZgAbB/PiUEDRaqXo2UU54gzJYOmlILM7yu8J/U2iJrIDK0MPc53xLJHzk+PzBRqWvVsW2PwsIqykV7sgRVgIHc4wN7+OZzdiTdKe5wnfWxYs4jx46MAxkfv81fk2FHekqf4P/dZUIJBtS1UiCjw8O2cuaGHWkMHYLUeo18PR3yQR9zKW/5cxhYXTArNRtQBwTLXQtQCh/EDYjGwnEqs/KY+Vss4933pET2HphnzG2m32t0YCQ4Epb9sAwxB0aqxN3IPD0kLtVpzMgOHG1pyXdhQ5 root@base-image.isucon.net"
    args[:metadata] = metadata.map{|key, value| "#{key}=#{value}"}.join(',')
    # Ubuntu 15.04 default sshd_config is configured as "PermitRootLogin without_password"
    # So once sshKeys configured, we can login into it without any more operations

    # If sshKeys configured, other normal user's keys are disabled.
    # We need to login into it by root always.

    args.delete(:verbose)

    instance = create_instance(project: project, name: name, args: args)
    instance_info_path.open('w') {|f| f.write instance.to_json }
    say_status 'run_instance', instance["name"]
  end

  desc 'provision', 'Provision the instance using ansible'
  def provision
    # ansible/_xxxx.yml is for special purpose
    # ansible/\d\d_xxxx.yml is for normal purpose
    playbooks = Dir.glob('ansible/*.yml').reject{|x| x =~ %r!/_[^/]+\.yml$! }.sort
    run_playbooks playbooks
  end

  desc 'cleanup', 'Delete log files from the instance'
  def cleanup
    run_playbooks 'ansible/_cleanup.yml'
  end

  desc 'terminate', 'Terminate the instance'
  def terminate
    instance = instance_info
    command = ['gcloud', 'compute', '--project', instance['project'], 'instances', 'delete', '--delete-disks', 'all', '--zone', instance['zone'], instance['name']].join(' ')
    say_status 'terminate', command
    exec command
  end

  desc 'create_image', 'Create GCP Image from the instance'
  def create_image
    raise NotImplementedError
    # Do it by yourself...
    # https://cloud.google.com/compute/docs/images#export_an_image_to_google_cloud_storage

    # gcloud compute disks create temporary-disk --zone ZONE
    # gcloud compute instances attach-disk example-instance --disk temporary-disk \
    #     --device-name temporary-disk \
    #     --zone ZONE
    # gcloud compute ssh example-instance
    # $ sudo mkdir /mnt/tmp
    # $ sudo /usr/share/google/safe_format_and_mount -m "mkfs.ext4 -F" /dev/sdb /mnt/tmp
    # $ sudo gcimagebundle -d /dev/sda -o /mnt/tmp/ --log_file=/tmp/abc.log
    # $ gsutil mb gs://BUCKET_NAM
    # $ gsutil cp /mnt/tmp/IMAGE_NAME.image.tar.gz gs://BUCKET_NAME
  end

  desc 'ssh', 'Login the instance via ssh'
  def ssh
    ## command below is to login as normal user name, but it's disabled by root sshKey
    # gcloud compute --project "isucon5-summer-course" ssh --zone "asia-east1-b" "image-test"

    ## use root key
    # ssh -i KEY_FILE -o UserKnownHostsFile=/dev/null -o CheckHostIP=no -o StrictHostKeyChecking=no USER@IP_ADDRESS
    ip = public_ip_address
    command = ['ssh', '-i', './keys/root_id_rsa', '-o', 'UserKnownHostsFile=/dev/null', '-o', 'CheckHostIP=no', '-o', 'StrictHostKeyChecking=no', "root@#{ip}"].join(' ')
    say_status 'exec', command
    exec command
  end

  no_tasks do
    def tmpdir
      dir = Pathname('../tmp').expand_path(__FILE__)
      dir.mkdir unless dir.directory?
      dir
    end

    def instance_info_path
      tmpdir.join('instance_info')
    end

    def instance_info
      @instance_info ||= begin
        JSON.parse(instance_info_path.read)
      end
    rescue Errno::ENOENT
      abort "#{instance_info_path} doesn't exist"
    end

    def create_instance(project:, name:, args: {})
      cmd = ['gcloud', '--format', 'json', 'compute', '--project', project, 'instances', 'create', name]
      cmd += build_cli_options(args)

      io = IO.popen(cmd)
      instance = JSON.parse(io.read).first rescue nil
      unless instance
        raise "failed to create instance"
      end

      # POST https://www.googleapis.com/compute/v1/projects/isucon5-qualify/zones/asia-east1-a/instances
      example = {
        "name": "instance-1",
        "zone": "https://www.googleapis.com/compute/v1/projects/isucon5-qualify/zones/asia-east1-a",
        "machineType": "https://www.googleapis.com/compute/v1/projects/isucon5-qualify/zones/asia-east1-a/machineTypes/n1-highcpu-4",
        "metadata": {
          "items": []
        },
        "tags": {
          "items": [
            "http-server"
          ]
        },
        "disks": [
          {
            "type": "PERSISTENT",
            "boot": true,
            "mode": "READ_WRITE",
            "deviceName": "instance-1",
            "autoDelete": true,
            "initializeParams": {
              "sourceImage": "https://www.googleapis.com/compute/v1/projects/ubuntu-os-cloud/global/images/ubuntu-1504-vivid-v20150616a",
              "diskType": "https://www.googleapis.com/compute/v1/projects/isucon5-qualify/zones/asia-east1-a/diskTypes/pd-standard",
              "diskSizeGb": "16"
            }
          }
        ],
        "canIpForward": false,
        "networkInterfaces": [
          {
            "network": "https://www.googleapis.com/compute/v1/projects/isucon5-qualify/global/networks/default",
            "accessConfigs": [
              {
                "name": "External NAT",
                "type": "ONE_TO_ONE_NAT"
              }
            ]
          }
        ],
        "description": "",
        "scheduling": {
          "preemptible": false,
          "onHostMaintenance": "MIGRATE",
          "automaticRestart": true
        },
        "serviceAccounts": [
          {
            "email": "default",
            "scopes": [
              "https://www.googleapis.com/auth/devstorage.read_only",
              "https://www.googleapis.com/auth/logging.write"
            ]
          }
        ]
      }

      create_http_firewall_rule(project: project, name: instance["networkInterfaces"].first["name"], target_tag: instance["tags"]["items"].first)

      instance.merge({'project' => project})
    end

    def create_http_firewall_rule(project:, name:, target_tag:)
      list = ['gcloud', '--format', 'json', 'compute', '--project', project, 'firewall-rules', 'list']
      JSON.parse(IO.popen(list).read).each do |rule|
        return if rule["name"] == name
      end

      cmd = ['gcloud', '--format', 'json', 'compute', '--project', project, 'firewall-rules', 'create', name]
      cmd += ['--allow', 'tcp:80', '--network', 'default', '--source-ranges', '0.0.0.0/0', '--target-tags', target_tag]

      JSON.parse(IO.popen(cmd).read)
    end

    def build_cli_options(args)
      options = []
      args.each do |key, value|
        options << '--' + key.to_s.gsub(/_/, '-')
        if value
          if value.is_a? Array
            options << value.map(&:to_s).join(',')
          else
            options << value.to_s
          end
        end
      end
      options
    end

    def public_ip_address
      @public_ip_address ||= begin
        instance_info['networkInterfaces'][0]["accessConfigs"][0]["natIP"]
      end
    end

    def run_playbooks(playbooks)
      # Run playbooks as root user
      playbooks = [playbooks] unless playbooks.is_a?(Array)
      opts = "-i '#{public_ip_address},'"
      opts += " --user=root"
      opts += " --private-key=keys/root_id_rsa"
      opts += " --verbose -vvvv" if options[:verbose]
      command = "ansible-playbook #{opts} #{playbooks.join(' ')}"
      say_status 'run', command
      system({'ANSIBLE_HOST_KEY_CHECKING' => 'False'}, command)
    end
  end
end

CLI.start
